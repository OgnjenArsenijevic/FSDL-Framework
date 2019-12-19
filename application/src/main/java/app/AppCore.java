package app;

import exceptions.*;
import impl.DropBoxTransferProvider;
import model.User;
import spec.TransferProvider;

import javax.sound.midi.Soundbank;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class AppCore {

    private Scanner sc;
    private TransferProvider transferProvider;

    public static void main(String[] args) {
        (new AppCore()).initProgram(args);
    }

    private void initProgram(String[] args)
    {
        String path = "";
        Scanner scanner = new Scanner(System.in);
        if (args == null || args.length == 0)
        {
            System.out.print("Enter name for remote storage root folder\n>");
            path = scanner.nextLine();
        }
        else
            path = args[0];
        transferProvider = new DropBoxTransferProvider(); ///or local
        System.out.println("Initializing storage...");
        boolean adminExists = false;
        try
        {
            adminExists = transferProvider.initStorage(path);
        }
        catch (InitStorageException e)
        {
            System.out.println(e.getMessage());
            initProgram(null);
        }
        boolean flag = false;
        if(!adminExists)
            System.out.println("Storage successfully created, you can register as admin user now");
        while(!adminExists)
        {
            flag = true;
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter admin username: ");
            String username = sc.nextLine();
            System.out.print("Enter admin password: ");
            String password = enterPassword();
            System.out.println("Creating admin...");
            try
            {
                transferProvider.registerAdmin(username,password);
                adminExists = true;
            }
            catch (InvalidFormatException | NetworkErrorException e)
            {
                System.out.println(e.getMessage());
            }
            catch (TerminalErrorException e)
            {
                System.out.println(e.getMessage());
                System.exit(0);
            }
        }
        if(!flag)
            System.out.println("Storage already exists, you can login now");
        else
            System.out.println("Admin successfully registered, you can login now");
        loop(transferProvider);
    }

    private void loop(TransferProvider transferProvider)
    {
        sc = new Scanner(System.in);
        String command = "waitInput";
        while (true) {
            System.out.print(">");
            command = sc.nextLine();
            if (command.equals("login"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter username: ");
                String username = sc.nextLine();
                System.out.print("Enter password: ");
                String password = enterPassword();
                System.out.println("Logging in...");
                try
                {
                    transferProvider.login(username,password);
                    System.out.println("Successfully logged in");
                }
                catch (InvalidFormatException | LoginException | UserException | NetworkErrorException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else  if (command.equals("list")){
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter what to list (absolute path) : ");
                String s = sc.nextLine();
                s = s.replace("\\","/");
                List<String> list = transferProvider.storageContent(s);
                System.out.println("Listing all files and folders...");
                if(list != null) {
                    for (int i = 0 ; i < list.size() ; i++){
                        String s1 = list.get(i);
                        if (s1 != null){
                            System.out.println(s1);
                        }
                    }
                }else {
                    System.out.println("Folder is empty");
                }
            }
            else if (command.equals("logout"))
            {
                System.out.println("Logging out...");
                try
                {
                    transferProvider.logout();
                    System.out.println("Successfully logged out");
                }
                catch (LogoutException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.equals("register"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter username: ");
                String username = sc.nextLine();
                System.out.print("Enter password: ");
                String password = enterPassword();
                System.out.println("Creating user...");
                try
                {
                    transferProvider.register(username,password);
                    System.out.println("User successfully created");
                }
                catch (InvalidFormatException | NetworkErrorException | UserException | RegisterException | PermissionException e)
                {
                    System.out.println(e.getMessage());
                }
                catch (TerminalErrorException e)
                {
                    System.out.println(e.getMessage());
                    System.exit(0);
                }
            }
            else if (command.equals("change-password"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter old password: ");
                String oldPassword = enterPassword();
                System.out.print("Enter new password: ");
                String newPassword = enterPassword();
                String username = User.getInstance().getUsername();
                System.out.println("Changing password...");
                try
                {
                    transferProvider.changePassword(username,oldPassword,newPassword);
                    System.out.println("Password successfully changed");
                }
                catch (InvalidFormatException | NetworkErrorException | UserException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.equals("change-permission"))
            {
                System.out.print("Enter username: ");
                String username = sc.nextLine();
                ArrayList<String> options = new ArrayList<>();
                options.add("createFolder");
                options.add("deleteFolder");
                options.add("uploadFolder");
                options.add("createFile");
                options.add("deleteFile");
                options.add("uploadFile");
                options.add("download");
                System.out.println("Choose permission number:");
                for (int i = 0; i < options.size(); i++)
                    System.out.println((i + 1) + " - " + options.get(i));
                String curr = sc.nextLine();
                ArrayList<Boolean> permissions = new ArrayList<>();
                ArrayList<Integer> indexes = new ArrayList<>();
                try
                {
                    while(true)
                    {
                        int chosen = Integer.parseInt(curr) - 1;
                        if (chosen < 0 || chosen >= options.size())
                        {
                            System.out.println("Invalid option chosen");
                            break;
                        }
                        System.out.println("Set " + options.get(chosen) + " t/f ? ");
                        curr = sc.nextLine();
                        if (!curr.equals("t") && !curr.equals("f"))
                        {
                            System.out.println("Invalid option chosen");
                            break;
                        }
                        if (curr.equals("t"))
                        {
                            permissions.add(true);
                            indexes.add(chosen);
                        }
                        else
                        {
                            permissions.add(false);
                            indexes.add(chosen);
                        }
                        System.out.println("Change another permision y/n ? ");
                        String tmp = sc.nextLine();
                        if(!tmp.equals("y") && !tmp.equals("n"))
                        {
                            System.out.println("Invalid option chosen");
                            break;
                        }
                        if(tmp.equals("n"))
                            break;
                        for (int i = 0; i < options.size(); i++)
                            System.out.println((i + 1) + " - " + options.get(i));
                        curr = sc.nextLine();
                    }
                }
                catch (NumberFormatException e)
                {
                    //no need to implement
                }
                System.out.println("Changing user permissions...");
                try
                {
                    transferProvider.changePermission(username,permissions,indexes);
                    System.out.println("Permissions successfully changed");
                }
                catch (NetworkErrorException | UserException | PermissionException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.startsWith("create-folder"))
            {
                String[] split = command.split(" ");
                ArrayList<String> parameters = new ArrayList<>();
                int option = 0;
                boolean stop = false;
                for(int i=1;i<split.length;i++)
                {
                    if(!split[i].equals("-c") && !split[i].equals("-o"))
                    {
                        try
                        {
                            if(option != 0)
                            {
                                System.out.println("Command " + command + " not recognised");
                                stop = true;
                                break;
                            }
                            option = Integer.parseInt(split[i]);
                        }
                        catch (NumberFormatException e)
                        {
                            stop = true;
                            System.out.println("Command " + command + " not recognised");
                            break;
                        }
                    }
                    else
                        parameters.add(split[i]);
                }
                if(stop)
                    continue;
                if(parameters.size()>2 || (parameters.size() == 2 && parameters.get(0).equals(parameters.get(1))))
                {
                    stop = true;
                    System.out.println("Command " + command + " not recognised");
                }
                if(stop)
                    continue;
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter destination path: ");
                String destinationPath = sc.nextLine();
                System.out.print("Enter folder name: ");
                String folderName = sc.nextLine();
                System.out.println("Creating folder...");
                try
                {
                    transferProvider.createFolder(option, destinationPath, folderName, parameters);
                    System.out.println((option < 2 ? "Folder" : "Folders")  + " successfully created");
                }
                catch (InvalidFormatException | UserException | PermissionException | FolderException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if(command.contains("forbid-extension"))
            {
                String split[] = command.split(" ");
                if(split.length!=2 || !split[0].equals("forbid-extension"))
                    System.out.println("Command " + command + " not recognised");
                else
                {
                    if(split[1].contains("."))
                        System.out.println(". not allowed in extension name");
                    else
                    {
                        System.out.println("Forbidding extension...");
                        try
                        {
                            transferProvider.forbidExtension(split[1]);
                            System.out.println("Extension successfully forbidden");
                        }
                        catch (InvalidFormatException | NetworkErrorException | UserException | PermissionException | FileException e)
                        {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
            else if (command.startsWith("create-file"))
            {
                String[] split = command.split(" ");
                ArrayList<String> parameters = new ArrayList<>();
                int option = 0;
                boolean stop = false;
                for(int i=1;i<split.length;i++)
                {
                    if(!split[i].equals("-c") && !split[i].equals("-o") && !split[i].equals("-m"))
                    {
                        try
                        {
                            if(option != 0)
                            {
                                System.out.println("Command " + command + " not recognised");
                                stop = true;
                                break;
                            }
                            option = Integer.parseInt(split[i]);
                        }
                        catch (NumberFormatException e)
                        {
                            stop = true;
                            System.out.println("Command " + command + " not recognised");
                        }
                    }
                    else
                        parameters.add(split[i]);
                }
                if(stop)
                    continue;
                int m=0,c=0,o=0;
                for(int i=0;i<parameters.size();i++)
                {
                    if(parameters.get(i).equals("-m"))
                        m++;
                    if(parameters.get(i).equals("-o"))
                        o++;
                    if(parameters.get(i).equals("-c"))
                        c++;
                }
                if(m > 1 || c > 1 || o > 1 || option<0)
                {
                    stop = true;
                    System.out.println("Command " + command + " not recognised");
                }
                if(stop)
                    continue;
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter destination path: ");
                String destinationPath = sc.nextLine();
                System.out.print("Enter file name: ");
                String fileName = sc.nextLine();
                String splt[] = fileName.split("\\.");
                try
                {
                    ArrayList<HashMap<String,String> > list = new ArrayList<>();
                    if(parameters.contains("-m"))
                    {
                        for(int i=1;i<=option;i++)
                            list.add(enterMetadata(splt[0] + i + "." + splt[1]));
                        if(option == 0)
                            list.add(enterMetadata(fileName));
                    }
                    System.out.println("Creating file...");
                    transferProvider.createFile(option, destinationPath, fileName, parameters, list);
                    System.out.println((option<2 ? "File" : "Files") + " successfully created");
                    if(parameters.contains("-m"))
                        System.out.println("File metadata successfully created");
                }
                catch (InvalidFormatException | NetworkErrorException | UserException | PermissionException | CreateFailedException | FolderException | FileException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.startsWith("upload-file"))
            {
                String[] split = command.split(" ");
                ArrayList<String> parameters = new ArrayList<>();
                int option = 0;
                boolean stop = false;
                for(int i=1;i<split.length;i++)
                {
                    if(!split[i].equals("-c") && !split[i].equals("-o") && !split[i].equals("-m"))
                    {
                        try
                        {
                            if(option != 0)
                            {
                                System.out.println("Command " + command + " not recognised");
                                stop = true;
                                break;
                            }
                            option = Integer.parseInt(split[i]);
                        }
                        catch (NumberFormatException e)
                        {
                            stop = true;
                            System.out.println("Command " + command + " not recognised");
                        }
                    }
                    else
                        parameters.add(split[i]);
                }
                if(stop)
                    continue;
                int m=0,c=0,o=0;
                for(int i=0;i<parameters.size();i++)
                {
                    if(parameters.get(i).equals("-m"))
                        m++;
                    if(parameters.get(i).equals("-o"))
                        o++;
                    if(parameters.get(i).equals("-c"))
                        c++;
                }
                if(m > 1 || c > 1 || o > 1 || option<0)
                {
                    stop = true;
                    System.out.println("Command " + command + " not recognised");
                }
                if(stop)
                    continue;
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter destination path: ");
                String destinationPath = sc.nextLine();
                ArrayList<String> userPCfilePaths = new ArrayList<>();
                ArrayList<String> fileNames = new ArrayList<>();
                if(option == 0)
                {
                    System.out.print("Enter file path with name: ");
                    String userPCfilePath = sc.nextLine();
                    System.out.print("Enter new file name : ");
                    String fileName = sc.nextLine();
                    userPCfilePaths.add(userPCfilePath);
                    fileNames.add(fileName);
                }
                for(int i=1;i<=option;i++)
                {
                    System.out.println("File: " + i);
                    System.out.print("Enter file path with name: ");
                    String userPCfilePath = sc.nextLine();
                    System.out.print("Enter new file name : ");
                    String fileName = sc.nextLine();
                    userPCfilePaths.add(userPCfilePath);
                    fileNames.add(fileName);
                }
                try
                {
                    ArrayList<HashMap<String,String> > list = new ArrayList<>();
                    if(parameters.contains("-m"))
                    {
                        for(int i=1;i<=option;i++)
                            list.add(enterMetadata(fileNames.get(i-1)));
                        if(option == 0)
                            list.add(enterMetadata(fileNames.get(0)));
                    }
                    System.out.println("Uploading file...");
                    transferProvider.uploadFile(option, destinationPath, userPCfilePaths, fileNames, parameters, list);
                    System.out.println((option<2 ? "File" : "Files") + " successfully uploaded");
                    if(parameters.contains("-m"))
                        System.out.println("File metadata successfully uploaded");
                }
                catch (UploadFailedException | UserException | InvalidFormatException | FileException | FolderException | NetworkErrorException | PermissionException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.equals("delete-folder"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter folder path: ");
                String folderPath = sc.nextLine();
                System.out.print("Enter folder name: ");
                String folderName = sc.nextLine();
                System.out.println("Deleting folder...");
                try
                {
                    transferProvider.deleteFolder(folderPath, folderName);
                    System.out.println("Folder successfully deleted");
                }
                catch (InvalidFormatException | UserException | PermissionException | FolderException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.equals("delete-file"))
            {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter file path: ");
                String filePath = sc.nextLine();
                System.out.print("Enter file name: ");
                String fileName = sc.nextLine();
                System.out.println("Deleting file...");
                try
                {
                    transferProvider.deleteFile(filePath, fileName);
                    System.out.println("File successfully deleted");
                }
                catch (InvalidFormatException | UserException | PermissionException | DeleteFailedException | FileException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.startsWith("zip-collection"))
            {
                String[] split = command.split(" ");
                ArrayList<String> parameters = new ArrayList<>();
                int option = 0;
                boolean stop = false;
                for(int i=1;i<split.length;i++)
                {
                    if(!split[i].equals("-c") && !split[i].equals("-o") && !split[i].equals("-m"))
                    {
                        try
                        {
                            if(option != 0)
                            {
                                System.out.println("Command " + command + " not recognised");
                                stop = true;
                                break;
                            }
                            option = Integer.parseInt(split[i]);
                        }
                        catch (NumberFormatException e)
                        {
                            stop = true;
                            System.out.println("Command " + command + " not recognised");
                        }
                    }
                    else
                        parameters.add(split[i]);
                }
                if(stop)
                    continue;
                int m=0,c=0,o=0;
                for(int i=0;i<parameters.size();i++)
                {
                    if(parameters.get(i).equals("-m"))
                        m++;
                    if(parameters.get(i).equals("-o"))
                        o++;
                    if(parameters.get(i).equals("-c"))
                        c++;
                }
                if(m > 1 || c > 1 || o > 1 || option<0)
                {
                    stop = true;
                    System.out.println("Command " + command + " not recognised");
                }
                if(stop)
                    continue;
                System.out.print("Enter file name (or automatic if you want to generate random): ");
                Scanner sc = new Scanner(System.in);
                String zipName = sc.nextLine();
                if(zipName.trim().equals("automatic"))
                {
                    long timestamp = System.currentTimeMillis()/1000;
                    zipName = timestamp + "";
                    System.out.println("Random chosen name is: " +  zipName);
                }
                ArrayList<String> userPCfilePaths = new ArrayList<>();
                for(int i=1;i<=option;i++)
                {
                    System.out.println("File " + i + ": ");
                    System.out.print("Enter file path with name: ");
                    String userPCfilePath = sc.nextLine();
                    userPCfilePaths.add(userPCfilePath);
                }
                System.out.print("Enter destination path: ");
                String destinationPath = sc.nextLine();
                HashMap<String,String> hashMap = new HashMap<>();
                if(parameters.contains("-m"))
                    hashMap = enterMetadata(zipName);
                System.out.println("Zipping folder...");
                try
                {
                    transferProvider.uploadZip(option,zipName, userPCfilePaths, destinationPath, parameters, hashMap);
                    System.out.println("Folder successfully zipped and uploaded");
                }
                catch (InvalidFormatException | NetworkErrorException | PermissionException | UserException | FolderException | FileException e)
                {
                    System.out.println(e.getMessage());
                }
            }
            else if (command.equals("download-folder")) {
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter local path: ");
                String localPath = sc.nextLine();
                System.out.print("Enter remote path: ");
                String remotePath = sc.nextLine();
                try
                {
                    System.out.println("Downloading zip...");
                    transferProvider.downloadZip(remotePath, localPath);
                    System.out.println("Zip successfully downloaded");
                }
                catch (InvalidFormatException | UserException | PermissionException e)
                {
                    System.out.println(e.getMessage());
                }


            }
            else if (command.startsWith("download"))
            {
                boolean metadataFlag = false;
                if(command.equals("download -m"))
                    metadataFlag = true;
                else
                {
                    if(!command.equals("download"))
                        continue;
                }
                Scanner sc = new Scanner(System.in);
                System.out.print("Enter local path: ");
                String localPath = sc.nextLine();
                System.out.print("Enter remote path: ");
                String remotePath = sc.nextLine();
                ArrayList<String> parameters = new ArrayList<>();
                if(metadataFlag)
                    parameters.add("-m");
                try
                {
                    System.out.println("Downloading file...");
                    transferProvider.downloadFile(remotePath,localPath,parameters);
                    System.out.println("File successfully downloaded");
                    if(metadataFlag)
                        System.out.println("File metadata successfully download (if there was any)");
                }
                catch (InvalidFormatException | UserException | PermissionException e)
                {
                    System.out.println(e.getMessage());
                }
            }else if (command.equals("help")) {
                help();
            }
            else if (command.equals("exit")){
                return;
            }else {
                System.out.println("Command " + command + " not recognised");
            }
        }
    }


    private void help() {
        System.out.println("Available functions: ");
        System.out.println("-m creates/downloads metadata");
        System.out.println("-o overrides");
        System.out.println("-c creates missing folders on path");
        System.out.println("number - how many things you want");
        System.out.println("login");
        System.out.println("logout");
        System.out.println("register");
        System.out.println("change-password");
        System.out.println("change-permission");
        System.out.println("create-folder number -c -o");
        System.out.println("zip-collection number -c -o -m");
        System.out.println("create-file number -c -o -m");
        System.out.println("upload-file number -c -o -m");
        System.out.println("delete-file");
        System.out.println("delete-folder");
        System.out.println("forbid-extenision ext");
        System.out.println("download -m -o");
        System.out.println("download-folder -o");
        System.out.println("help");
        System.out.println("exit");
    }

    private String enterPassword()
    {
        String password;
        Console console = System.console();
        if(console == null)
        {
            Scanner sc = new Scanner(System.in);
            password = sc.nextLine();
        }
        else
        {
            char[] arr = console.readPassword();
            password = String.copyValueOf(arr);
        }
        return password;
    }

    private HashMap<String,String> enterMetadata(String fileName)
    {
        System.out.println("Entering metadata for file: " + fileName);
        System.out.println("Enter exit as key and value to stop input");
        HashMap<String,String> hashMap = new HashMap<>();
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter key: ");
        String key = sc.nextLine();
        System.out.print("Enter value: ");
        String val = sc.nextLine();
        while(!(key.equals("exit") && val.equals("exit")))
        {
            if(key.trim().length()==0 || val.trim().length()==0)
                System.out.println("Key and value cannot be empty");
            else
                hashMap.put(key,val);
            System.out.print("Enter key: ");
            key = sc.nextLine();
            System.out.print("Enter value: ");
            val = sc.nextLine();
        }
        return hashMap;
    }
}
