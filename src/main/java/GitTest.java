import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: gitTest
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-29 10:56
 * @Description:
 */
public class GitTest {

    public static void main(String[] args) throws GitAPIException, IOException {

        String remoteRepositoryUrl = "https://github.com/fantastzjy/jgit";
        String cloneToLocalDir = "jgit";
        String localRepositoryPath = "D:/MyConfiguration/jiaying2.zhang/Desktop/git";
        String buildPathName = "D:/MyConfiguration/jiaying2.zhang/Desktop/ggg";

        String username = "fantastzjy";
        String password = "Fantastu7.";

        // 凭证管理
        CredentialsProvider provider = createCredential(username, password);

        //1、仓库相关

        // clone远程仓库
//        Git git = CloneRepositoryFromRemote(remoteRepositoryUrl, cloneToLocalDir, provider);

//        构建本地仓库
//        Repository repository = repositoryBuild(buildPathName);

        //获取本地仓库
        //方式一
        Repository repositoryFromDir = getRepositoryFromDir(localRepositoryPath);
        Git git = new Git(repositoryFromDir);
        //方式二
//        Git git = Git.open(new File(localRepositoryPath));   //√

        //2、commit   √
        commit(git, "测试提交", provider);

        //3、push
//        push(git, provider);

        System.out.println("结束");
    }

    //Http凭证
    //通过CredentialsProvider管理凭证，常用的是UsernamePasswordCredentialsProvider
    public static CredentialsProvider createCredential(String userName, String password) {
        return new UsernamePasswordCredentialsProvider(userName, password);
    }

    //仓库相关
    //构建仓库
    public static Repository repositoryBuild(String buildPathName) {
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setGitDir(new File(buildPathName))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            return repository;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //clone远程仓库
    //通过Git.cloneRepository -> clone远程仓库，如果需要凭证，则需要指定credentialsProvider
    public static Git CloneRepositoryFromRemote(String repoUrl, String cloneDir, CredentialsProvider provider) throws GitAPIException {
        //CloneCommand cloneCommand = Git.cloneRepository();
        //对cloneCommand再次进行了封装
        Git git = Git.cloneRepository()
                .setCredentialsProvider(provider)
                .setURI(repoUrl)
                .setDirectory(new File(cloneDir)).call();
        return git;
    }

    //若已克隆，读取已有仓库
    public static Repository getRepositoryFromDir(String dir) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(Paths.get(dir, ".git").toFile())
                .build();
    }

    //    commit
    //    git 命令： git commit -a -m '{msg}'
    //    commit前先add
    public static void commit(Git git, String message, CredentialsProvider provider) throws GitAPIException {
        // 添加文件
        git.add().addFilepattern(".").call();
        //提交
        git.commit()
                .setMessage(message)
                .call();

    }

    //    push
    //    git 命令： git push origin branchName    push直接调用push即可,需要指定credentialsProvider
    public static void push(Git git, CredentialsProvider provider) throws GitAPIException, IOException {
        push(git, null, provider);
    }

    public static void push(Git git, String branch, CredentialsProvider provider) throws GitAPIException, IOException {
        if (branch == null) {
            branch = git.getRepository().getBranch();
        }
        git.push()
                .setCredentialsProvider(provider)
                .setRemote("origin").setRefSpecs(new RefSpec(branch)).call();
    }


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
                    System.out.println("\n Commit-Message: " + revCommit.getFullMessage());
                }
                revWalk.dispose();
            }
        }

        return commits;
    }

}
