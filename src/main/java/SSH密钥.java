import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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
public class SSH密钥 {

    //私钥文件地址
    public static String keyPath = "D:/MyConfiguration/jiaying2.zhang/.ssh/id_rsa";

    //本项目的远程仓库
    public static String remoteRepoPath = "ssh://git@github.com/fantastzjy/git.git";

    //本项目的本地仓库
    public static String localRepoPath = "D:/MyConfiguration/jiaying2.zhang/Desktop/git";

    //测试仓库远程路径
//    public static String remoteRepositoryUrl = "https://github.com/fantastzjy/jgit";
//    String remoteRepoPath = "ssh://github.com/fantastzjy/jgit.git";   //错误地址 未加协议
    public static String remoteRepoPathTest = "ssh://git@github.com/fantastzjy/jgit.git"; //git地址

    //测试仓库本地路径
    public static String localRepoPathTest = "D:/MyConfiguration/jiaying2.zhang/Desktop/jgit";

    //************************* Main ****************************

    public static void main(String[] args) throws GitAPIException, IOException {
        //ssh session的工厂,用来创建密匙连接
        SshSessionFactory sshSessionFactory = getsshSessionFactory(keyPath);
        //克隆
//        gitClone(remoteRepoPathTest, localRepoPathTest, sshSessionFactory);

        //commit
        commit(localRepoPath, null, "测试提交 "+ new Random().nextInt(10));


        //push
        push(remoteRepoPath, localRepoPath, null, sshSessionFactory);
        System.out.println("push 结束");

        //pull
        pull(remoteRepoPath, localRepoPath, sshSessionFactory);


        //获取提交信息      仓库内(path下,有可能为仓库下子文件夹)的所有提交版本号
//        List<String> gitVersions = getGitVersions(localRepoPath);

        //读取仓库日志
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
                sch.addIdentity(keyPath); //添加私钥文件
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

        pushGit.add().addFilepattern(".").call();
        //提交
        pushGit.commit()
                .setMessage(message)
                .call();

        System.out.println("提交成功！");
    }

    //**************************** push ***********************************

    /**
     * http push
     * 自动获取分支名称
     *
     * @param git
     * @param branch
     * @param provider
     * @throws GitAPIException
     * @throws IOException
     */
    public static void push(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).call();
    }

    /**
     * ssh push
     *
     * @param remoteRepoPath
     * @param localRepoPath
     * @param branch
     * @param sshSessionFactory
     * @throws IOException
     * @throws GitAPIException
     */
    public static void push(String remoteRepoPath, String localRepoPath, String branch, SshSessionFactory sshSessionFactory) throws IOException, GitAPIException {
        //关联到本地仓库
        FileRepository fileRepository = new FileRepository(new File(localRepoPath));
        Git pushGit = new Git(fileRepository);

//        Git pushGit = Git.open(new File(localRepoPath));

        if (branch == null) {
            branch = pushGit.getRepository().getBranch();
        }

        pushGit.push()
//                .setRemote(remoteRepoPath)
//                .setRemote("origin/master")
                .setRefSpecs(new RefSpec("origin/master"))
                .setPushAll()
//                .setCredentialsProvider(provider)
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
            //1  报错 org.eclipse.jgit.api.errors.WrongRepositoryStateException: Cannot pull into a repository with state: BARE
            FileRepository fileRepository = new FileRepository(new File(localRepoPath));
            Git pullGit = new Git(fileRepository);

            //2  报错org.eclipse.jgit.transport.TransportHttp cannot be cast to org.eclipse.jgit.transport.SshTransport
//            Git pullGit = Git.open(new File(localRepoPath));

            //设置密钥,拉取文件
            PullCommand pullCommand = pullGit
                    .pull()
                    //用于拉取操作的远程（uri 或名称）。 如果没有设置远程，将使用分支的配置
                    // 如果分支配置缺少Constants.DEFAULT_REMOTE_NAME的默认值将被使用。
                    .setRemote("origin")
                    //用于拉取操作的远程分支名称。 如果没有设置 remoteBranchName，将使用分支的配置。
                    // 如果分支配置丢失，则使用与当前分支同名的远程分支。
                    .setRemoteBranchName("master")
                    .setTransportConfigCallback(
                            transport -> {
                                SshTransport sshTransport = (SshTransport) transport;
                                sshTransport.setSshSessionFactory(sshSessionFactory);
                            });

            pullCommand.call();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


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

    //************************************** 查看状态 *******************************

    /**
     * 查看状态
     */
    public static void status(String localRepoPath) throws IOException {
        //关联到本地仓库    下面两种获取git 结果不一致怎么回事？
        //法一 异常
//        FileRepository fileRepository = new FileRepository(new File(localRepoPath));
//        Git git = new Git(fileRepository);
        //法二 正常
        Git git = Git.open(new File(localRepoPath));

        try {
            Status status = git.status().call();
            System.out.println("读取仓库状态*****************");
            log.info("开始打印日志*************");
            log.info("Git Change: " + status.getChanged());
            log.info("Git Modified: " + status.getModified());
            log.info("Git UncommittedChanges: " + status.getUncommittedChanges());
            log.info("Git Untracked: " + status.getUntracked());
            log.info("打印日志结束*************");
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

    public static List<String> getLogsSinceCommit(Repository repository, String branch, String commit) throws IOException {
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


}