import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;

/**
 * @Package: PACKAGE_NAME
 * @ClassName: ssh
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-30 19:07
 * @Description:
 */
public class SSH {
    public static void main(String[] args) {

        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setPassword("Fantastu7.");
            }
        };

        CloneCommand cloneCommand = Git.cloneRepository();
//      cloneCommand.setURI("ssh://user@example.com/repo.git");
        cloneCommand.setURI("git@github.com:fantastzjy/git.git");

        cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
            @Override
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        });
    }
}
