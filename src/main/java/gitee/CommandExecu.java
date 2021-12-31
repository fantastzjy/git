package gitee;

/**
 * @Package: gitee
 * @ClassName: CommandExecu
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-31 16:54
 * @Description:
 */
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecu {
    @Before
    public void init() throws IOException {
    }

    @Test
    public void execu() throws IOException {
        Process result = Runtime.getRuntime().exec("git");
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));
        StringBuilder sbu = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sbu.append(line + "\n");
        }
        System.out.println(sbu);
    }

    @Test
    public void process() throws IOException {
        String cmd = "git";
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        //在linux平台下，默认使用PIPE，发现父进程退出后，子进程也退出了，具体原因可能是PIPE的读取一端关闭，导致写入的一端在写入时收到SIGPIPE信号，这个信号导致子进程退出。
        //在linux平台下，ProcessBuilder.inheritIO()来使产生的子进程继承父进程的标准io，发现执行时，子进程的输出和父进程一样，都输出到控制台，但是父进程退出之后，子进程会继续执行。
        //processBuilder.inheritIO();
        Process result = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(result.getInputStream()));
        StringBuilder sbu = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sbu.append(line + "\n");
        }
        System.out.println(sbu);
    }

    @Test
    public void array() throws Exception {
        String[] arr = new String[4];
        arr.clone();
        System.out.println(arr instanceof Object);
        System.out.println(arr.hashCode());
        System.out.println(arr.getClass().getName());
        System.out.println(arr.getClass().getSuperclass().getName());
        String[][] arr1 = new String[1][1];
        System.out.println(arr1 instanceof Object);
        System.out.println(arr1.hashCode());
        System.out.println(arr1.getClass().getName());
        System.out.println(arr1.getClass().getSuperclass().getName());
        int a[] = {1, 9};
        System.out.println(a.toString());
        Class<?> aClass = Class.forName("[[Ljava.lang.String;");
        System.out.println(aClass.isLocalClass());
    }
}
