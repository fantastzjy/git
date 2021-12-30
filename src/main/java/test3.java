import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.junit.Test;

import java.io.File;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: test3
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-30 15:38
 * @Description:
 */
public class test3 {


    @Test
    public static void TestClone() {
        String localPath = "D:/git";
        String url = "https://github.com/fantastzjy/jgit";

        cloneRepository(url, localPath);
    }

    public static String cloneRepository(String url, String localPath) {
        try {
            System.out.println("开始下载......");

            CloneCommand cc = Git.cloneRepository().setURI(url);
            cc.setDirectory(new File(localPath)).call();

            System.out.println("下载完成......");

            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }



}
