import config.GitRepositoryConfig;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.List;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: TestGit
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-29 18:28
 * @Description: https://blog.csdn.net/Ryan_5410/article/details/98505795
 */

public class SignIn {
    //    @Autowired
    public static GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig();

    public static String username = "fantastzjy";
    public static String password = "Fantastu7.";


    public static String pathName = "D:/MyConfiguration/jiaying2.zhang/Desktop/my/git/directory";
//    Repository repository = GitTest.repositoryBuild(pathName);
//        System.out.println(repository.getDirectory());

    String repoUrl = "https://github.com/fantastzjy/jgit";
    String cloneDir = "jgit";


    public static void main(String[] args) throws GitAPIException {

//        gitRepositoryConfig.setGitUsername(username);
//        gitRepositoryConfig.setGitPassword(password);
//
//
//        CredentialsProvider credentialsProvider = reloadAllowHosts();
//
//        CloneCommand cloneCommand = Git.cloneRepository();
//        cloneCommand.setCredentialsProvider(credentialsProvider);
//
//        System.out.println();


        String localRepoPath = "D:/repo";
        String localRepoGitConfig = "D:/repo/.git";
        String remoteRepoURI = "git@github.com/fantastzjy/jgit";
        String localCodeDir = "D:/platplat";


        //建立与远程仓库的联系，仅需要执行一次
        Git git = Git.cloneRepository().setURI(remoteRepoURI).setDirectory(new File(localRepoPath)).call();
        System.out.println();

    }

    public static CredentialsProvider reloadAllowHosts() {
        return new UsernamePasswordCredentialsProvider(gitRepositoryConfig.getGitUsername(), gitRepositoryConfig.getGitPassword());
    }


}
