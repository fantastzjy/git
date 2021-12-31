import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: ssh
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-30 19:07
 * @Description:
 */
public class SSH {
    public static void main(String[] args) {
         String keyPath = "D:/MyConfiguration/jiaying2.zhang/.ssh/id_rsa";  //私钥文件

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setPassword("Fantastu7.");
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch sch = super.createDefaultJSch(fs);
                sch.addIdentity(keyPath); //添加私钥文件
                return sch;
            }

        };



//        CloneCommand cloneCommand = Git.cloneRepository();
////      cloneCommand.setURI("ssh://user@example.com/repo.git");
//        cloneCommand.setURI("ssh://github.com:fantastzjy/jgit");

//        cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
//            @Override
//            public void configure(Transport transport) {
//                SshTransport sshTransport = (SshTransport) transport;
//                sshTransport.setSshSessionFactory(sshSessionFactory);
//            }
//        });

//        try (Git call = cloneCommand.call()) {
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }

        System.out.println("结束..........");

    }
}
