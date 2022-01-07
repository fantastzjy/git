package org.dstadler.jgit.porcelain;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.FetchResult;

import java.io.File;
import java.io.IOException;
import java.util.List;



/**
 * Simple snippet which shows how to fetch commits from a remote Git repository
 *
 * @author dominik.stadler at gmx.at
 */
public class FetchRemoteCommitsWithPrune {

    private static final String REMOTE_URL = "https://github.com/github/testrepo.git";

    public static void main(String[] args) throws IOException, GitAPIException, InterruptedException {
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try (Git git = Git.cloneRepository()
                .setURI(REMOTE_URL)
                .setDirectory(localPath)
                .call()) {
            // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
            System.out.println("Having repository: " + git.getRepository().getDirectory());

            System.out.println("Starting fetch");
            FetchResult result = git.fetch().setCheckFetchedObjects(true).call();
            System.out.println("Messages: " + result.getMessages());

            // ensure master/HEAD are still there
            System.out.println("Listing local branches:");
            List<Ref> call = git.branchList().call();
            for (Ref ref : call) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }

            System.out.println("Now including remote branches:");
            call = git.branchList().setListMode(ListMode.ALL).call();
            for (Ref ref : call) {
                System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
            }
        }

        // clean up here to not keep using more and more disk-space for these samples
        try {
            FileUtils.deleteDirectory(localPath);
        } catch (IOException e) {
            System.out.println("Retrying deleting path " + localPath + " once after catching exception");
            e.printStackTrace();

            Thread.sleep(1000);

            FileUtils.deleteDirectory(localPath);
        }
    }
}
