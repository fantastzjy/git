import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.Test;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: SSH密钥
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-31 10:08
 * @Description:
 */
@Slf4j
public class SSHGit {

    //私钥文件地址
    public static String keyPath = "D:/MyConfiguration/jiaying2.zhang/.ssh/id_rsa";

    //测试仓库远程路径
//    public static String remoteRepositoryUrl = "https://github.com/fantastzjy/jgit";
    public static String remoteRepoPathTest = "ssh://git@github.com/fantastzjy/jgit.git"; //git地址
    public static String localRepoPathTest = "D:/MyConfiguration/jiaying2.zhang/Desktop/jgit";

    //地址设为本项目
//    public static String remoteRepoPath = "ssh://git@github.com/fantastzjy/git.git";
//    public static String localRepoPath = "D:/MyConfiguration/jiaying2.zhang/Desktop/git";

    //地址设为测试项目
    public static String remoteRepoPath = remoteRepoPathTest;
    public static String localRepoPath = localRepoPathTest;


    //************************* Main ****************************

    public static void main(String[] args) throws GitAPIException, IOException {
        //ssh session的工厂,用来创建密匙连接
        SshSessionFactory sshSessionFactory = getsshSessionFactory(keyPath);
        //克隆
//        gitClone(remoteRepoPathTest, localRepoPathTest, sshSessionFactory);
//
//        FileRepository fileRepository = new FileRepository(new File(SSHGit.localRepoPath));
//        Git git = new Git(fileRepository);
//
//        System.err.println("Listing local branches:");
//        List<Ref> call = git.branchList().call();


        //************************* commit **************   实现
//        status(localRepoPath);//打印提交前状态
        commit(localRepoPath, "qqq", "测试提交 " + new Random().nextInt(10));

//        status(localRepoPath);//打印提交后状态


        //************************** push ******************  实现
//        push( localRepoPath, null, sshSessionFactory);  //将当前分支 push
//        push( localRepoPath, "qqq", sshSessionFactory); //将qqq  分支 push
        push(localRepoPath, "qqq", sshSessionFactory); //将main 分支 push

        //************************** pull ****************** 实现
        //总结
        // 对于本地修改的、在文件夹直接删除的
        // 在pull时 如果没有其他分支进行操作过的文件
        // 如果在本地删除未提交 下拉时删除的文件不会再出现，因为之前文件就是自己改过的，下拉 merge时 不用再出现
//        pull(remoteRepoPath, localRepoPath, sshSessionFactory);


        //************************ 获取分支信息 ************* 实现
//        getBranchList();

        //切换分支
//        checkout(localRepoPath, "main", sshSessionFactory);
//        checkout(localRepoPath, "qqq",sshSessionFactory);
//        checkout(localRepoPath, "t2",sshSessionFactory);
        //打印分支信息
        getBranchList();

        //获取提交信息   可以实现
        // 仓库内(path下,有可能为仓库下子文件夹)的所有提交版本号
//        List<String> gitVersions = getGitVersions(localRepoPath);

        //读取仓库日志   可以实现
//        List<String> logs = getLogs(Git.open(new File(localRepoPath)).getRepository());

        //读取仓库状态
        status(localRepoPath);

        System.out.println("测试结束");
    }

    /**
     * 创建密匙连接
     * ssh session的工厂,用来创建密匙连接
     *
     * @param keyPath
     * @return
     */
    public static SshSessionFactory getsshSessionFactory(String keyPath) {
        //方式一：ssh session的工厂,用来创建密匙连接
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                //添加私钥文件
                sch.addIdentity(keyPath);
                return sch;
            }
        };

//        方式二：ssh密钥+密码
//        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
//            @Override
//            protected void configure(OpenSshConfig.Host host, Session session) {
//                session.setConfig("StrictHostKeyChecking", "no");
//                session.setPassword("Fantastu7.");
//            }
//
//            @Override
//            protected JSch createDefaultJSch(FS fs) throws JSchException {
//                JSch sch = super.createDefaultJSch(fs);
//                sch.addIdentity(keyPath); //添加私钥文件
//                return sch;
//            }
//        };

        return sshSessionFactory;
    }


    /**
     * localRepoPath 为本地文件夹
     * keyPath 私钥文件 path
     * remoteRepoPath 为 ssh git远端仓库地址
     *
     * @param remoteRepoPath
     * @param localRepoPath
     * @param sshSessionFactory
     */
    protected static void gitClone(String remoteRepoPath, String localRepoPath, SshSessionFactory sshSessionFactory) {
        System.out.println("===" + remoteRepoPath + "===" + localRepoPath + "===" + keyPath);
        //克隆代码库命令
        CloneCommand cloneCommand = Git.cloneRepository();
        Git git = null;
        try {
            git = cloneCommand.setURI(remoteRepoPath) //设置远程URI
                    .setTransportConfigCallback(transport -> {
                        SshTransport sshTransport = (SshTransport) transport;
                        sshTransport.setSshSessionFactory(sshSessionFactory);
                    })
                    .setDirectory(new File(localRepoPath)) //设置下载存放路径
                    .call();
            System.out.println("Clone successfully !!!!!!!!!!!!!!!!!!!!!!");
        } catch (Exception e) {
            System.err.println("Clone fail !!!!!!!!!!!!!!");
            e.printStackTrace();
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }

    //************************* commit ************************

    /**
     * commit
     * git 命令： git commit -a -m '{msg}'
     * commit前先add
     *
     * @param localRepoPath
     * @param message
     * @throws GitAPIException
     */
    public static void commit(String localRepoPath, String branch, String message) throws GitAPIException, IOException {

        // 添加文件
        Git pushGit = Git.open(new File(localRepoPath));

        if (branch == null) {
            branch = pushGit.getRepository().getBranch();
        }

        //todo  切换到想要提交的分支  不然就是当前分支
        checkout(localRepoPath,branch);

        //将所有 add
        pushGit.add().addFilepattern(".").call();

        //提交   不能设置分支 因为commit  在哪个分支commit就会把所有修改的提交到当前分支
        pushGit.commit()
                .setAll(true) //若不设置  setAll(true)  commit的将只是新添加的文件  对于修改的不进行commit
                .setMessage(message)
                .call();

        System.out.println("Commit 成功！");
    }

    //**************************** push ***********************************

    /**
     * ssh push
     *
     * @param localRepoPath
     * @param pushBranch
     * @param sshSessionFactory
     * @throws IOException
     * @throws GitAPIException
     */
    public static void push(String localRepoPath, String pushBranch, SshSessionFactory sshSessionFactory) throws IOException, GitAPIException {
        //关联到本地仓库
        Git pushGit = Git.open(new File(localRepoPath));

        //获取分支信息
        if (pushBranch == null) {
            pushBranch = pushGit.getRepository().getBranch();
        }

        pushGit.push()
                .setRemote("origin")
                .setRefSpecs(new RefSpec(pushBranch))
//                .setPushAll()
//                .setCredentialsProvider(provider)  //http提供权限方式
                .setTransportConfigCallback(
                        transport -> {
                            SshTransport sshTransport = (SshTransport) transport;
                            sshTransport.setSshSessionFactory(sshSessionFactory);
                        })
                .call();
    }


    //**************************** pull ***********************************

    /**
     * pull 项目
     * localRepoPath 为 .git 的 path 如 : D:\\gitRepository\\.git
     *
     * @param remoteRepoPath
     * @param localRepoPath
     * @param sshSessionFactory
     */
    public static void pull(String remoteRepoPath, String localRepoPath, SshSessionFactory sshSessionFactory) {
        System.out.println("===" + remoteRepoPath + "===" + localRepoPath + "===" + keyPath);

        try {
            //关联到本地仓库
            // 报错 org.eclipse.jgit.api.errors.WrongRepositoryStateException: Cannot pull into a repository with state: BARE
//            Git pullGit = new Git(new FileRepository(new File(localRepoPath)));
//            Git pullGit = new Git(new FileRepository(localRepoPath));
            Git pullGit = Git.open(new File(localRepoPath));

            //设置密钥,拉取文件
            pullGit
                    .pull()
                    .setRebase(false)
                    //用于拉取操作的远程（uri 或名称）。 如果没有设置远程，将使用分支的配置
                    // 如果分支配置缺少Constants.DEFAULT_REMOTE_NAME的默认值将被使用。
                    .setRemote("origin")
                    //用于拉取操作的远程分支名称。 如果没有设置 remoteBranchName，将使用分支的配置。
                    // 如果分支配置丢失，则使用与当前分支同名的远程分支。
                    .setRemoteBranchName("qqq")
                    .setTransportConfigCallback(
                            transport -> {
                                SshTransport sshTransport = (SshTransport) transport;
                                sshTransport.setSshSessionFactory(sshSessionFactory);
                            })
                    .call();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //**************************** checkout ***********************************

    /**
     * checkout
     *
     * @param localRepoPath
     * @param checkoutBranchName
     * @throws IOException
     * @throws GitAPIException
     */
    public static void checkout(String localRepoPath, String checkoutBranchName) throws IOException, GitAPIException {

        Git git = Git.open(new File(localRepoPath));
        //获取当前分支名称
        String preBranch = git.getRepository().getBranch();

        //*************** 需要先获取远程分支信息************************
//        FetchResult fetchResult = git.fetch()
//
//                .setRemote(REMOTE_URL)
//                .setRefSpecs("+refs/pull/6/head:master")
//                .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))

        //这里不需要权限验证即可
//                .setTransportConfigCallback(
//                        transport -> {
//                            SshTransport sshTransport = (SshTransport) transport;
//                            sshTransport.setSshSessionFactory(sshSessionFactory);
//                        })

//                .call();

//        System.err.println("Result when fetching the PR: " + fetchResult.getMessages());


        //分支切换 checkout

        //切换到现有分支
        git.checkout().setName(checkoutBranchName).call();

        //创建并切换
//        git.checkout().setCreateBranch(true).setName("newbranch").call();

        //从远程分支创建一个新的跟踪分支并checkout
//        git.checkout().setCreateBranch(true).setName("stable_"+System.currentTimeMillis())
//                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
//                .setStartPoint("origin/qqq")
//                .call();

        /**
         分支切换方法举例

         //checkout现有分支：
         git.checkout().setName("feature").call();

         //从索引中查看路径： Check out paths from the index:
         git.checkout().addPath("file1.txt").addPath("file2.txt").call();

         //从提交中查看路径： Check out a path from a commit:
         git.checkout().setStartPoint("HEADˆ").addPath("file1.txt").call();

         //创建一个新分支并检查它：Create a new branch and check it out:
         git.checkout().setCreateBranch(true).setName("newbranch").call();

         //为远程分支创建一个新的跟踪分支并检查它：Create a new tracking branch for a remote branch and check it out:
         git.checkout()
         .setCreateBranch(true)
         .setName("stable")
         .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
         .setStartPoint("origin/stable")
         .call();
         **/


        String currBranch = git.getRepository().getBranch();
        //获取当前分支名称
        System.err.println("切换分支前： " + preBranch + '\n' + "切换分支后： " + currBranch);


        /*

        //获取分支列表
        List<Ref> call = git.branchList()
                .setListMode(ListBranchCommand.ListMode.ALL)
                .call();

        //在分支列表中获取主分支
        Ref remoteMaster = null;
        for (Ref ref : call) {
            if (ref.getName().contains("master")) {
                remoteMaster = ref;
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }

        System.out.println("切换分支");

        git.checkout()
                .setCreateBranch(false)
                //.setStartPoint(buildRevCommit(git.getRepository(), remoteMaster.getObjectId()))
                //.setStartPoint("origin/master")
                //.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                //将主分支切换到分支 qqq \ master
//                    .setName("master")
                .setName("master")
//                .setProgressMonitor(new SimpleProgressMonitor())
                .call();


        for (Ref ref : git.branchList()
                .setListMode(ListBranchCommand.ListMode.ALL)
                .call()) {
            System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
*/


    }

    /**
     * 分支切换创建的静态内部类
     */
    private static class SimpleProgressMonitor implements ProgressMonitor {
        @Override
        public void start(int totalTasks) {
            System.out.println("Starting work on " + totalTasks + " tasks");
        }

        @Override
        public void beginTask(String title, int totalWork) {
            System.out.println("Start " + title + ": " + totalWork);
        }

        @Override
        public void update(int completed) {
            System.out.print(completed + "-");
        }

        @Override
        public void endTask() {
            System.out.println("Done");
        }

        @Override
        public boolean isCancelled() {
            return false;
        }
    }

    //************************************** 查看状态 *******************************

    /**
     * 查看状态
     */
    public static void status(String localRepoPath) throws IOException {
        //关联到本地仓库    下面两种获取git 结果不一致怎么回事？
        //法一 异常
//        Git git = new Git(new FileRepository(new File(localRepoPath));
        //法二 正常
        Git git = Git.open(new File(localRepoPath));

        try {
            Status status = git.status().call();
            log.info("*************");
            log.info("仓库状态信息：");
            log.info("Git Change: " + status.getChanged());
            log.info("Git Modified: " + status.getModified());
            log.info("Git UncommittedChanges: " + status.getUncommittedChanges());
            log.info("Git Untracked: " + status.getUntracked());
            log.info("*************");
        } catch (Exception e) {
            log.info(e.getMessage());
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }


    //*************************************** 读取仓库日志 ****************************
    //    读取仓库日志
    //    可以通过RevWalk读取仓库日志。
    //    revWalk.parseCommit 可读取一条commit
    //    遍历revWalk，可读取所有日志
    public static List<String> getLogs(Repository repository) throws IOException {
        return getLogsSinceCommit(repository, null, null);
    }

    public static List<String> getLogsSinceCommit(Repository repository, String commit) throws IOException {
        return getLogsSinceCommit(repository, null, commit);
    }

    public static List<String> getLogsSinceCommit(Repository repository, String branch, String commit) throws
            IOException {
        if (branch == null) {
            branch = repository.getBranch();
        }
        Ref head = repository.findRef("refs/heads/" + branch);
        List<String> commits = new ArrayList<>();
        if (head != null) {
            try (RevWalk revWalk = new RevWalk(repository)) {
                revWalk.markStart(revWalk.parseCommit(head.getObjectId()));
                for (RevCommit revCommit : revWalk) {
                    if (revCommit.getId().getName().equals(commit)) {
                        break;
                    }
                    commits.add(revCommit.getFullMessage());
                    System.err.println("\n Commit-Message: " + revCommit.getFullMessage());
                }
                revWalk.dispose();
            }
        }

        return commits;
    }
    //获取提交日志： 也可以实现
    /**

     System.err.println("Checked out PR, now printing log, it should include two commits from the PR on top");

     Iterable<RevCommit> logs = git.log()
     .call();
     int i = 1;
     for (RevCommit rev : logs) {
     //push的记录 无论是否有修改内容
     System.err.println("提交日志： Commit: " + i++ + " " + rev + ", name: " + rev.getName() + ", id: " + rev.getId().getName());
     }
     */


    //*******************JGit 获取提交信息/详细提交日志*************************

    /**
     * 1.获取提交信息
     * 此方法获取了仓库内(path下,有可能为仓库下子文件夹)的所有提交版本号
     */
    public static List<String> getGitVersions(String path) {
        List<String> versions = new ArrayList<>();
        try {
            Git git = Git.open(new File(path));
//            Repository repository = git.getRepository();
//            Git git1 = new Git(repository);
            Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                System.out.println("LogCommit: " + commit);
                System.out.println("===" + commit.getFullMessage());
                String version = commit.getName(); //版本号,用来查询详细信息
                versions.add(version);
                System.out.println("===" + commit.getName());
                System.out.println("===" + commit.getAuthorIdent());
                count++;
            }
            return versions;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 2、获取两个版本间提交详细记录
     * version 为 上一个方法查询出来的版本号
     * @param
     */
//    public static void showDiff(String path,String oldVersion,String newVersion) {
//        try {
//            Git git = Git.open(new File(path));
//            Repository repository = git.getRepository();
//            //旧版本
//            AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldVersion);
//            //新版本
//            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newVersion);
//
//            List<DiffEntry> diff = git.diff().
//                    setOldTree(oldTreeParser).
//                    setNewTree(newTreeParser).
//                    call();
//            for (DiffEntry entry : diff) {
//                System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
//                //此处可传一个输出流获取提交详情
//                DiffFormatter formatter = new DiffFormatter(System.out);
//                formatter.setRepository(repository);
//                formatter.format(entry);
//            }
//
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /**
     * 获取分支列表
     *
     * @param
     * @throws IOException
     * @throws GitAPIException
     */

    public static void getBranchList() throws IOException, GitAPIException {

//        Repository Repository = new FileRepositoryBuilder().readEnvironment() // scan environment GIT_* variables
//                .findGitDir() // scan up the file system tree
//                .build();
//        ;

//        FileRepository fileRepository = new FileRepository(new File(SSHGit.localRepoPath));   //不能获取到仓库的所有信息
//        FileRepository fileRepository = new FileRepository(SSHGit.localRepoPath + "/.git");
//        Git git = new Git(fileRepository);

        Git git = Git.open(new File(SSHGit.localRepoPath));

        //本地分支列表:
        List<Ref> call = git.branchList().call();
        for (Ref ref : call) {
            System.err.println("本地分支: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
        System.out.println();
        //远程分支列表:
        List<Ref> call1 = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call();
        for (Ref ref : call1) {
            System.err.println("远程分支: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
        System.out.println();

    }


}