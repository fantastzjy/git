package gitee.utils;

/**
 * @Package: gitee.utils
 * @ClassName: GitUtils
 * @Author: jiaying2.zhang
 * @CreateTime: 2021-12-31 16:55
 * @Description:
 */

import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import java.io.IOException;

public class GitUtils {
    /**
     * 打印配置
     */
    public static void printStoredConfig(StoredConfig config) {
        config.getSections().forEach(s -> {
            System.out.println(s + " ->");
            config.getSubsections(s).forEach(su -> {
                System.out.println("  " + su + "  -->");
                config.getNames(s, su).forEach(n -> {
                    System.out.println("    " + n + ":" + config.getString(s, su, n));
                });
            });
        });
    }

    /**
     * 构造一个RevCommit
     */
    public static RevCommit buildRevCommit(Repository repository, ObjectId objectId) throws IOException {
        return repository.parseCommit(objectId);
    }

    /**
     * 查找对应name分支
     * shortName eg:
     * Remote -> /origin/master
     * local -> master
     */
    public static Ref findBranch(Repository repository, String shortName, boolean isRemote) throws IOException {
        Ref ref = null;
        if (isRemote) {
            ref = repository.exactRef(Constants.R_REMOTES + shortName);
        } else {
            ref = repository.exactRef(Constants.R_HEADS + shortName);
        }
        return ref;
    }
}
