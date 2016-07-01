import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

public class ExtendedFileUtil extends FileUtil {
    private String[] getFilesAndDirectories(String fileOrDirList, boolean recursively
            , boolean getDirectories, boolean getFiles) throws IOException {
        Configuration configuration = new Configuration();
        String root = configuration.get("fs.default.name");

        ArrayList<String> arraylist = new ArrayList<String>();

        Stack<Path> stack = new Stack<Path>();
        String uri = null;

        FileSystem fs1 = null;
        String[] fileOrDir = fileOrDirList.split(",", -1);
        for (String aFileOrDir : fileOrDir) {
            if (aFileOrDir.indexOf(root) == -1) {
                uri = root + aFileOrDir;
            } else {
                uri = aFileOrDir;
            }
            FileSystem fs = FileSystem.get(URI.create(uri), configuration);

            Path[] paths = new Path[1];
            paths[0] = new Path(uri);
            FileStatus[] status = fs.listStatus(paths);
            for (FileStatus statu : status) {
                if (statu.isDir()) {
                    stack.push(statu.getPath());
                    if (getDirectories) {
                        arraylist.add(statu.getPath().toString());
                    }
                } else {
                    if (getFiles) {
                        arraylist.add(statu.getPath().toString());
                    }
                }
            }

            if (recursively) {
                Path p1 = null;
                FileStatus[] status1 = null;
                while (!stack.empty()) {
                    p1 = stack.pop();
                    fs1 = FileSystem.get(URI.create(p1.toString()), configuration);
                    paths[0] = new Path(p1.toString());
                    status1 = fs1.listStatus(paths);

                    for (FileStatus aStatus1 : status1) {
                        if (aStatus1.isDir()) {
                            stack.push(aStatus1.getPath());
                            if (getDirectories) {
                                arraylist.add(aStatus1.getPath().toString());
                            }
                        } else {
                            if (getFiles) {
                                arraylist.add(aStatus1.getPath().toString());
                            }
                        }
                    }
                }
            }
            fs.close();
        }
        arraylist.trimToSize();
        String[] returnArray = new String[arraylist.size()];

        return arraylist.toArray(returnArray);
    }

    /**
     * @param fileOrDir   Comma delimited list of input files or directories in HDFS. Input can be given with HDFS URL.
     *                    i.e. "hdfs://hd4.ev1.yellowpages.com:9000/user/directory" and "/user/directory"  means the same
     * @param recursively When set to "true" then recursively opens all sub directories and returns files
     */
    public String[] getFilesOnly(String fileOrDir, boolean recursively) throws IOException {
        return this.getFilesAndDirectories(fileOrDir, recursively, false, true);
    }

    /**
     * Same as String[] getFilesOnly(String fileOrDir, boolean recursively) except that it only returns paths
     * that match the regex
     */
    public String[] getFilesOnly(String fileOrDir, boolean recursively, String regex) throws IOException {
        ArrayList<String> arraylist = new ArrayList<String>();
        String[] tempArr = this.getFilesOnly(fileOrDir, recursively);
        Pattern p = Pattern.compile(".*" + regex + ".*");
        // Extract the file names that match the regex
        for (String aTempArr : tempArr) {
            if (p.matcher(aTempArr).matches()) {
                arraylist.add(aTempArr);
            }
        }
        arraylist.trimToSize();
        String[] returnArray = new String[arraylist.size()];
        returnArray = arraylist.toArray(returnArray);
        return returnArray;
    }


    /**
     * @param fileOrDir   Comma delimited list of input files or directories in HDFS. Input can be given with HDFS URL.
     *                    i.e. "hdfs://hd4.ev1.yellowpages.com:9000/user/directory" and "/user/directory"  means the same
     * @param recursively When set to "true" then recursively opens all sub directories and returns sub directories
     */
    public String[] getDirectoriesOnly(String fileOrDir, boolean recursively) throws IOException {
        return this.getFilesAndDirectories(fileOrDir, recursively, true, false);
    }

    /**
     * @param fileOrDir   Comma delimited list of input files or directories in HDFS. Input can be given with HDFS URL.
     *                    i.e. "hdfs://hd4.ev1.yellowpages.com:9000/user/directory" and "/user/directory"  means the same
     * @param recursively When set to "true" then recursively opens all sub directories and returns files and sub directories
     */
    public String[] getFilesAndDirectories(String fileOrDir, boolean recursively) throws IOException {
        return this.getFilesAndDirectories(fileOrDir, recursively, true, true);
    }

    /**
     * This method uses recursion to retrieve a list of files/directories
     *
     * @param p             Path to the directory or file you want to start at.
     * @param configuration Configuration
     * @param files         a Map<Path,FileStatus> of path names to FileStatus objects.
     * @throws IOException
     */
    public void getFiles(Path p, Configuration configuration, Map<Path, FileStatus> files) throws IOException {
        FileSystem fs = FileSystem.get(p.toUri(), configuration);
        if (files == null) {
            files = new HashMap();
        }
        if (fs.isFile(p)) {
            files.put(p, fs.getFileStatus(p));
        } else {
            FileStatus[] statuses = fs.listStatus(p);
            for (FileStatus s : statuses) {
                if (s.isDir()) {
                    getFiles(s.getPath(), configuration, files);
                } else {
                    files.put(s.getPath(), s);
                }
            }
        }
        fs.close();
    }

    /**
     * This method deletes all zero byte files within a directory and all its subdirectories
     *
     * @param fileOrDir If file then delete the file if its zero bytes, if directory then delete
     *                  all zero bytes files from the directory
     */
    public void removeAllZeroByteFiles(String fileOrDir) {
        try {
            Configuration configuration = new Configuration();
            Map<Path, FileStatus> files = new HashMap<Path, FileStatus>();
            this.getFiles(new Path(fileOrDir), configuration, files);
            for (Path p : files.keySet()) {
                FileStatus s = files.get(p);
                if (s.getLen() == 0) {
                    FileSystem fs = FileSystem.get(p.toUri(), configuration);
                    fs.delete(p, false);
                    fs.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the size of file or a directory in HDFS.
     *
     * @param fileOrDir file or diretory or list of files or directories in HDFS, if directory then size of all
     *                  files within the directory and its subdirectories are returned
     * @return size of the file or directory (sum of all files in the directory and sub directories)
     */
    public long size(String fileOrDir) throws IOException {
        long totalSize = 0;
        Configuration configuration = new Configuration();
        String allFiles[] = fileOrDir.split(",", -1);

        for (String allFile : allFiles) {
            Path p = new Path(allFile);
            FileSystem fs = FileSystem.get(p.toUri(), configuration);
            totalSize = totalSize + fs.getContentSummary(p).getLength();
            fs.close();
        }
        return totalSize;
    }

    /**
     * The method moves a single or multiple files or directories, if exists, to trash.
     * It also accepts list of hdfs file or directory delimited by comma.
     *
     * @param fileOrDir HDFS file or directory name or list of HDFS file or directory names
     * @throws IOException
     */

    public void removeHdfsPath(String fileOrDir)
            throws IOException {
        Configuration configuration = new Configuration();
        FileSystem fs = FileSystem.newInstance(URI.create(fileOrDir), configuration);
        String[] fileList = fileOrDir.split(",", -1);
        Trash trash = new Trash(configuration);
        trash.expunge();
        for (String aFileList : fileList) {
            Path p = new Path(aFileList);
            if (fs.exists(p)) {
                trash.moveToTrash(p);
            }
        }
        fs.close();
    }
}
