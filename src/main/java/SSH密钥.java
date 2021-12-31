import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: SSH密钥
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-31 10:08
 * @Description:
 */
public class SSH密钥 {

    //私钥文件地址
    public static String keyPath = "D:/MyConfiguration/jiaying2.zhang/.ssh/id_rsa";

    //本项目的远程仓库
    public static String localRepositoryPath = "D:/MyConfiguration/jiaying2.zhang/Desktop/git";

    //测试仓库远程路径
//    public static String remoteRepositoryUrl = "https://github.com/fantastzjy/jgit";
//    String remoteRepoPath = "ssh://github.com/fantastzjy/jgit.git";   //错误地址 未加协议
    public static String remoteRepoPath = "ssh://git@github.com/fantastzjy/jgit.git"; //git地址

    //测试仓库本地路径
    public static String cloneToLocalDir = "D:/MyConfiguration/jiaying2.zhang/Desktop/test1";


    public static void main(String[] args) {
        //克隆
        gitClone(remoteRepoPath, cloneToLocalDir, keyPath);


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
     * @param keyPath
     */
    protected static void gitClone(String remoteRepoPath, String localRepoPath, String keyPath) {
        SshSessionFactory sshSessionFactory = getsshSessionFactory(keyPath);

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
            System.out.println("Clone successfully!!!!!!!!!!!!!!!!!!!!!!");
        } catch (Exception e) {
            System.out.println("fail");
            e.printStackTrace();
        } finally {
            if (git != null) {
                git.close();
            }
        }
    }


    //localRepoPath 为 .git 的 path 如 : D:\\gitRepository\\.git
    //keyPath 私钥文件 path
    public static void pullCode(String remoteRepoPath, String localRepoPath, String keyPath) {
        System.out.println("===" + remoteRepoPath + "===" + localRepoPath + "===" + keyPath);
        //ssh session的工厂,用来创建密匙连接
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                sch.addIdentity(keyPath);
                return sch;
            }
        };
        try {
            //关联到本地仓库
            FileRepository fileRepository = new FileRepository(new File(localRepoPath));
            Git pullGit = new Git(fileRepository);
            //设置密钥,拉取文件
            PullCommand pullCommand = pullGit
                    .pull()
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
            Repository repository = git.getRepository();
            Git git1 = new Git(repository);
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
     * @param path
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


}