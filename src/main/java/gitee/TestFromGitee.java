package gitee; /**
 * @Package: PACKAGE_NAME
 * @ClassName: gitee.TestFromGitee
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-31 16:53
 * @Description:
 */
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Mine
 * @description jgit 简单操作验证
 * @create 2020-05-15 17:44
 */
public class TestFromGitee {
    String remoteUrl = "https://gitee.com/yebukong/scratchpad.git"; //远程测试地址
    File targetDir = new File("D:\\Tmp\\scratchpad");
    File targetGitDir = new File(targetDir, Constants.DOT_GIT);
    Properties properties;
    UsernamePasswordCredentialsProvider up;

    @Before
    public void init() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(new File("D:\\Tmp\\config\\mine.properties")));
        up = new UsernamePasswordCredentialsProvider(properties.getProperty("gitee.name"), properties.getProperty("gitee.password"));
    }

    /**
     * clone远程项目到本地
     */
    @Test
    public void cloneRepository() throws GitAPIException, InterruptedException {
        //clone必须是空目录，包括隐藏目录
        try (Git git = Git.cloneRepository()
                .setCredentialsProvider(up) //认证信息
                .setURI(remoteUrl)
                .setDirectory(targetDir)
                .setBranch("features")
                .call()) {

            System.out.println("clone成功: " + git.getRepository().getDirectory());
//            printStoredConfig(git.getRepository().getConfig());

        }
    }

    /**
     * init目录
     */
    @Test
    public void createNewRepository() throws GitAPIException {
        try (Git git = Git.init()
                .setDirectory(targetDir).call()) {
            System.out.println("init成功: " + git.getRepository().getDirectory());
//            printStoredConfig(git.getRepository().getConfig());
        }
    }

    /**
     * 打开已有Repository
     */
    @Test
    public void openRepository() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
//            printStoredConfig(git.getRepository().getConfig());

        }
    }

    /**
     * 获取分支信息
     * ListMode ->
     * null:仅本地
     * REMOTE:远程
     * ALL:所有
     */
    @Test
    public void branchList() throws IOException, GitAPIException {

        try (Git git = new Git(new FileRepository(targetGitDir))) {
            List<Ref> call = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            for (Ref ref : call) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }
    }

    @Test
    public void findRef() throws IOException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            //
            Ref ref = git.getRepository().findRef("origin/features");
            System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
    }

    @Test
    public void createBranch() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
            System.out.println("--------------------------------");
            git.branchCreate()
                    //.setStartPoint(buildRevCommit(git.getRepository(), remoteMaster.getObjectId()))//使用RevCommit方式创建分支，默认NOTRACK
                    .setStartPoint("origin/master")  //使用分支名方式创建分支，默认TRACK
                    .setName("mastertest1")
                    //.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
                    .call();
            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }
    }

    @Test
    public void pushBranch() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            git.push().setCredentialsProvider(up).call();
        }
    }

    /**
     * 强制更新远程分支列表：类似于git remote update origin --prune
     */
    @Test
    public void updateBranch() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            FetchResult result = git.fetch().setCredentialsProvider(up)
                    .setRemoveDeletedRefs(true)
                    .setCheckFetchedObjects(true).call();

            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }

        }
    }

    @Test
    public void deleteBranch() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
            //已检出分支无法删除  ,远程分支删除无法生效...
            List<String> call = git.branchDelete().setBranchNames("origin/test2").setForce(true).call();
//            RefSpec refSpec = new RefSpec().setSource(null).setDestination(Constants.R_HEADS+demand.getDemandBranch());
//            git.push().setRefSpecs(refSpec).setRemote(Constants.DEFAULT_REMOTE_NAME).call();
//            git.push().setCredentialsProvider(up).setPushOptions(Collections.singletonList("delete origin/test2")).call();
            call.forEach(System.out::println);
            System.out.println("--------------------------------");
            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }
    }

    @Test
    public void checkout() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            List<Ref> call = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.REMOTE)
                    .call();
            Ref remoteMaster = null;
            for (Ref ref : call) {
                if (ref.getName().contains("master")) {
                    remoteMaster = ref;
                    System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
                }
            }
            git.checkout()
                    .setCreateBranch(false)
                    //.setStartPoint(buildRevCommit(git.getRepository(), remoteMaster.getObjectId()))
                    //.setStartPoint("origin/master")
                    //.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setName("mastertest1")
                    .call();
            for (Ref ref : git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }
    }

    @Test
    public void pull() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            git.pull()
                    .setCredentialsProvider(up)
                    .setRebase(true)
                    .call();
        }

    }

    @Test
    public void commit() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            //添加 git管理
//            git.add()
//                    .addFilepattern("test3.txt")
//                    .call();
            git.add().addFilepattern(".").call();
            git.commit().setAll(true).setMessage("auto x").setCommitter("yebukong", "yebukong@qq.com").call();

        }
    }

    @Test
    public void push() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            git.push()
                    .setCredentialsProvider(up)
                    .call();
        }
    }

    @Test
    public void currBranch() throws IOException, GitAPIException {
        try (Git git = new Git(new FileRepository(targetGitDir))) {
            System.out.println(git.getRepository());
            Set<String> remoteNames = git.getRepository().getRemoteNames();
            remoteNames.forEach(System.out::println);
            System.out.println(git.getRepository().getBranch());
            System.out.println(git.getRepository().getFullBranch());
        }
    }

}
