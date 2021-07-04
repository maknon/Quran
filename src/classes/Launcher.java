package classes;

import java.io.*;
import java.nio.channels.*;

public class Launcher
{
    public static void main(String[] args)
    {
        final String lineSeparator = System.getProperty("line.separator");

        try
        {
            final String programFolder = new File(Launcher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath()+"/"; // This will be used when you need to create a file with reference to the main class path. ClassLoader.getResource() will not work since it will return null

            Thread.sleep(1000);

            //final ServerSocket lockSocket = new ServerSocket(23947); Throw Windows firewall warning.
            final FileChannel lockChannel = new RandomAccessFile(programFolder+"temp/lockFile", "rw").getChannel();
            final FileLock lock = lockChannel.tryLock();

            if(lock == null)
            {
                lockChannel.close();

                final RandomAccessFile logFile = new RandomAccessFile(programFolder+"temp/logFile", "rw");
                final FileChannel logChannel = logFile.getChannel();
                final FileLock logLock = logChannel.lock(); // will be blocked until it can lock the file for writing
                logFile.seek(logFile.length());

                for(String arg : args)
                    // If the file wasn't written with writeUTF() you can't read it with readUTF() in Quran
                    logFile.writeUTF(arg+lineSeparator);

                logLock.release();
                logChannel.close();
                logFile.close();
            }
            else
            {
                // Between releasing the lock here and acquiring it by Quran, another Launcher might be start e.g. when clicking multiple files at the same time for the first time before running the application. That's why not all the selected files will be displayed to the user.
                lock.release();
                lockChannel.close();
                if(Quran.isMAC)
                {
                    final String[] arg = new String[args.length+1];
                    arg[0]=new File(programFolder+"quran_mac.sh").getAbsolutePath();
                    System.arraycopy(args, 0, arg, 1, args.length);
                    Runtime.getRuntime().exec(arg); // To allow Dock icon and the arabic title name.
                }
                else
                {
                    final String[] arg = new String[args.length+5];
                    arg[0]=System.getProperty("java.home")+File.separator+"bin"+File.separator+"java";
                    arg[1]="-Dsun.java2d.noddraw=true";
                    arg[2]="-Dsun.java2d.ddscale=true";
                    arg[3]="-jar";
                    arg[5]=new File(programFolder+"Quran.jar").getAbsolutePath();
                    System.arraycopy(args, 0, arg, 5, args.length);
                    Runtime.getRuntime().exec(arg);
                }
            }
        }
        catch(Exception e){e.printStackTrace();}
    }
}
