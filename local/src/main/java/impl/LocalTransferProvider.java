package impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import exceptions.*;
import model.FolderZiper;
import model.Permission;
import model.User;
import spec.TransferProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class LocalTransferProvider extends AbstractLocalProvider implements TransferProvider
{
    private String ROOT_DIRECTORY_PATH = "";
    private boolean adminExists = false;

    @Override
    public boolean initStorage(String absolutePath) throws InitStorageException
    {
        if (absolutePath == null || absolutePath.length() == 0)
            throw new InitStorageException("Error, empty path");
        absolutePath = absolutePath.replace("\\", "/");
      /*  if (absolutePath.charAt(0) != '/')
            absolutePath = "/" + absolutePath;*/
        if (absolutePath.charAt(absolutePath.length() - 1) == '/')
            absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
       /* int cnt = 0;
        for (int i = 0; i < absolutePath.length(); i++)
            if (absolutePath.charAt(i) == '/')
                cnt++;
        if (cnt > 1)
            throw new InitStorageException("Error, invalid storage name");*/
        absolutePath = absolutePath.replace("/", File.separator);
        ROOT_DIRECTORY_PATH = absolutePath;
        if (new File(ROOT_DIRECTORY_PATH).exists())
        {
            adminExists = true;
            return true;
        }
        if (!create(ROOT_DIRECTORY_PATH, null))
        {
            throw new InitStorageException("Error while initializing storage");
        }
        try
        {
            boolean success;
            File file = new File("userData.storage");
            file.createNewFile();
            success = upload("userData.storage", "userData.storage");
            file.delete();
            if(!success)
            {
                delete(ROOT_DIRECTORY_PATH);
                ROOT_DIRECTORY_PATH = "";
                throw new InitStorageException("Error while initializing storage");
            }
            file = new File("userPermissions.storage");
            file.createNewFile();
            success = upload("userPermissions.storage", "userPermissions.storage");
            if(!success)
            {
                delete(ROOT_DIRECTORY_PATH);
                ROOT_DIRECTORY_PATH = "";
                throw new InitStorageException("Error while initializing storage");
            }
            file.delete();
            file = new File("forbiddenExtensions.storage");
            file.createNewFile();
            success = upload("forbiddenExtensions.storage", "forbiddenExtensions.storage");
            file.delete();
            if(!success)
            {
                delete(ROOT_DIRECTORY_PATH);
                ROOT_DIRECTORY_PATH = "";
                throw new InitStorageException("Error while initializing storage");
            }
            return false;
        }
        catch (IOException e)
        {
            delete(ROOT_DIRECTORY_PATH);
            ROOT_DIRECTORY_PATH = "";
            throw new InitStorageException("Error while initializing storage");
        }
    }

    @Override
    public void registerAdmin(String username, String password) throws InvalidFormatException, NetworkErrorException, TerminalErrorException
    {
        checkUser(username, password);
        if (!download("userData.storage", "userData.storage"))
            throw new NetworkErrorException("Error while accessing storage database");
        File userData = new File("userData.storage");
        if (!download("userPermissions.storage", "userPermissions.storage"))
        {
            userData.delete();
            throw new NetworkErrorException("Error while accessing storage database");
        }
        File userPermissions = new File("userPermissions.storage");
        if (userData == null || !userData.exists() || userPermissions == null || !userPermissions.exists())
            throw new NetworkErrorException("Error while accessing storage database");
        try
        {
            boolean success;
            String user = username + "///" + hashPassword(password, "SHA-512");
            FileWriter fw = new FileWriter(userData, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(user);
            pw.close();
            fw.close();
            success = upload("userData.storage", "userData.storage");
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            success = writePermissionForUser(username,true,userPermissions);
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            success = upload("userPermissions.storage","userPermissions.storage");
            int cnt = 0;
            while(!success && cnt<30)
            {
                success = upload("userPermissions.storage","userPermissions.storage");
                cnt++;
            }
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new TerminalErrorException("Error while setting admin status for the profile, please contact Dropbox administrator");
            }
            userData.delete();
            userPermissions.delete();
        }
        catch (IOException e)
        {
            userData.delete();
            userPermissions.delete();
            throw new NetworkErrorException("Error while accessing storage database");
        }
        adminExists = true;
        return;
    }
    @Override
    public void login(String username, String password) throws InvalidFormatException, NetworkErrorException, UserException, LoginException
    {
        if (User.isCreated())
            throw new UserException("Error, somebody already logged in");
        checkUser(username, password);
        if (!download("userData.storage", "userData.storage"))
            throw new NetworkErrorException("Error while accessing storage database");
        File userData = new File("userData.storage");
        if (!download("userPermissions.storage", "userPermissions.storage"))
        {
            userData.delete();
            throw new NetworkErrorException("Error while accessing storage database");
        }
        File userPermissions = new File("userPermissions.storage");
        if (userData == null || !userData.exists() || userPermissions == null || !userPermissions.exists())
            throw new NetworkErrorException("Error while accessing storage database");
        Scanner sc = null;
        try
        {
            sc = new Scanner(userData);
            int cnt = 0;
            while (sc.hasNextLine())
            {
                cnt++;
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username))
                {
                    if (split[1].equals(hashPassword(password, "SHA-512")))
                    {
                        try
                        {
                            Permission perm = this.getPermissionForUser(username, userPermissions);
                            User.getInstance().init(username, perm);
                            if (cnt == 1)
                                User.getInstance().setAdmin(true);
                            sc.close();
                            userData.delete();
                            userPermissions.delete();
                            return;
                        }
                        catch (UserException e)
                        {
                            sc.close();
                            userData.delete();
                            userPermissions.delete();
                            throw e;
                        }
                    }
                    else
                    {
                        sc.close();
                        userData.delete();
                        userPermissions.delete();
                        throw new LoginException("Error, wrong password entered");
                    }
                }
            }
            sc.close();
            userData.delete();
            userPermissions.delete();
        }
        catch (FileNotFoundException e)
        {
            sc.close();
            userData.delete();
            userPermissions.delete();
        }
        throw new LoginException("Error, username not found in database");
    }

    @Override
    public void logout() throws LogoutException
    {
        if (User.isCreated())
        {
            User.destroyUser();
            return;
        }
        throw new LogoutException("Error, user already logged out");
    }

    @Override
    public void register(String username, String password) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, TerminalErrorException, RegisterException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().isAdmin())
            throw new PermissionException("Error, you dont have permission to do registration");
        checkUser(username, password);
        if (!download("userData.storage", "userData.storage"))
            throw new NetworkErrorException("Error while accessing storage database");
        File userData = new File("userData.storage");
        if (!download("userPermissions.storage", "userPermissions.storage"))
        {
            userData.delete();
            throw new NetworkErrorException("Error while accessing storage database");
        }
        File userPermissions = new File("userPermissions.storage");
        if (userData == null || !userData.exists() || userPermissions == null || !userPermissions.exists())
            throw new NetworkErrorException("Error while accessing storage database");
        Scanner sc = null;
        try
        {
            sc = new Scanner(userData);
            while (sc.hasNextLine())
            {
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username))
                {
                    sc.close();
                    userData.delete();
                    userPermissions.delete();
                    throw new RegisterException("Error, username already exists");
                }
            }
            sc.close();
        }
        catch (FileNotFoundException e)
        {
            sc.close();
            userData.delete();
            userPermissions.delete();
            throw new RegisterException("Error while creating user");
        }
        try
        {
            boolean success;
            String user = username + "///" + hashPassword(password, "SHA-512");
            FileWriter fw = new FileWriter(userData, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(user);
            pw.close();
            fw.close();
            success = upload("userData.storage", "userData.storage");
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            success = writePermissionForUser(username,false,userPermissions);
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            success = upload("userPermissions.storage","userPermissions.storage");
            int cnt = 0;
            while(!success && cnt<30)
            {
                success = upload("userPermissions.storage","userPermissions.storage");
                cnt++;
            }
            if(!success)
            {
                userData.delete();
                userPermissions.delete();
                throw new TerminalErrorException("Error while setting the profile, please contact Dropbox administrator");
            }
            userData.delete();
            userPermissions.delete();

        }
        catch (IOException e)
        {
            throw new NetworkErrorException("Error while accessing storage database");
        }
        return;
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws InvalidFormatException, NetworkErrorException, UserException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if(newPassword.trim().equals(""))
            throw new InvalidFormatException("Error, new password is empty");
        String newLine = username;
        newLine += "///";
        newLine += hashPassword(newPassword, "SHA-512");
        String oldLine = username;
        oldLine += "///";
        oldLine += hashPassword(oldPassword, "SHA-512");
        if (!download("userData.storage", "userData.storage"))
            throw new NetworkErrorException("Error while accessing storage database");
        File userData = new File("userData.storage");
        if (replaceLine(userData, oldLine, newLine))
        {
            boolean success = upload("userData.storage", "userData.storage");
            if(!success)
            {
                userData.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            userData.delete();
            return;
        }
        throw new UserException("Error, wrong password entered");
    }

    @Override
    public void changePermission(String username, ArrayList<Boolean> changedPermissions, ArrayList<Integer> indexes) throws NetworkErrorException, UserException, PermissionException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().isAdmin())
            throw new PermissionException("Error, you dont have permission to change permissions");
        ArrayList<Boolean> permissions;
        File userPermissions = null;
        ArrayList<Boolean> oldUserPremissions = new ArrayList<>();
        if (username.equals(User.getInstance().getUsername()))
            throw new PermissionException("Error, permissions for admin cannot be changed");
        try
        {
            if (!download("userPermissions.storage", "userPermissions.storage"))
                throw new NetworkErrorException("Error while accessing storage database");
            userPermissions = new File("userPermissions.storage");
            permissions = getPermissionForUser(username, userPermissions).getPermissions();
            for (Boolean pp : permissions)
                oldUserPremissions.add(pp);
        }
        catch (NullPointerException e)
        {
            userPermissions.delete();
            return ;
        }
        String oldLine = username;
        oldLine += "///";
        Permission p = new Permission();
        p.setPermissions(oldUserPremissions);
        oldLine += p.toString();
        String newLine = username;
        newLine += "///";
        for(int i=0;i<changedPermissions.size();i++)
            oldUserPremissions.set(indexes.get(i),changedPermissions.get(i));
        p.setPermissions(oldUserPremissions);
        newLine += p.toString();
        if (replaceLine(userPermissions, oldLine, newLine))
        {
            boolean success = upload("userPermissions.storage", "userPermissions.storage");
            if(!success)
            {
                userPermissions.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            userPermissions.delete();
            return;
        }
        userPermissions.delete();
        throw new UserException("Error, user doesn't exist in database");
    }

    @Override
    public void createFolder(int num, String destinationPath, String folderName, ArrayList<String> parameters) throws InvalidFormatException, UserException, PermissionException, FolderException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isCreateFolder())
            throw new PermissionException("Error, you dont have permission to create folder");
        destinationPath = getAbsolutePathForPath(destinationPath);
        if (destinationPath.trim().length() == 0)
            throw new InvalidFormatException("Error, destination path not entered");
        if (folderName.trim().length() == 0)
            throw new InvalidFormatException("Error, folder name not entered");
        if (destinationPath.contains(".") || destinationPath.endsWith(File.separator))
            throw new InvalidFormatException("Error, invalid folder path");
        if(destinationPath.contains(File.separator + File.separator))
        for (int i = 0; i < destinationPath.length() - 1; i++)
            throw new InvalidFormatException("Error, invalid folder path");
        if (folderName.contains("/") || folderName.contains("\\") || folderName.contains(".") || folderName.contains(File.separator))
            throw new InvalidFormatException("Error, invalid folder name");
        String fullPathAndName = destinationPath + File.separator + folderName;
        File file = new File(destinationPath);
        if(!file.exists())
        {
            if (parameters != null && !parameters.contains("-c"))
                throw new FolderException("Error, some folders from destination path are missing");
            if(!file.mkdirs())
                throw new FolderException("Error while creating missing folders");
        }
        if(num == 0)
            create(fullPathAndName,parameters);
        for(int i=1;i<=num;i++)
            create(fullPathAndName + i, parameters);
        return;
    }

    @Override
    public void uploadZip(int num, String zipName, ArrayList<String> userPCfilePaths, String destinationPath, ArrayList<String> parameters, HashMap<String,String> metadata) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, FolderException, FileException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isCreateFolder())
            throw new PermissionException("Error, you dont have permission to upload zip file");
        if(zipName.contains("/") || zipName.contains("\\") || zipName.contains(".") || zipName.contains(File.separator))
            throw new InvalidFormatException("Error, invalid name exception");
        File zipFile = new File(zipName);
        zipFile.mkdir();
        for(int i=1;i<=num;i++)
        {
            File currFile = new File(userPCfilePaths.get(i-1));
            String userPCfilePath = userPCfilePaths.get(i-1);
            if(currFile.isDirectory())
            {
                deleteDirectory(zipFile);
                throw new FileException("Error, current file is directory");
            }
            try
            {
                String tmp = zipName;
                int idx=-1;
                for(int j=0;j<userPCfilePath.length();j++)
                {
                    if(userPCfilePath.charAt(j)=='/' || userPCfilePath.charAt(j)=='\\' || userPCfilePath.equals(File.separator))
                        idx=j;
                }
                tmp+="/";
                for(int j=idx+1;j<userPCfilePath.length();j++)
                    tmp+=userPCfilePath.charAt(j);
                Files.copy(currFile.toPath(),new File(tmp).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e)
            {
                deleteDirectory(zipFile);
                throw new FileException("Error while copying the file");
            }
        }
        try
        {
            FolderZiper.zipFolder(zipName,zipName + ".zip");
        }
        catch (Exception e)
        {
            deleteDirectory(zipFile);
            throw new FolderException("Error while zipping folder");
        }
        try
        {
            if(parameters.contains("-c"))
                new File(getAbsolutePathForPath(destinationPath)).mkdirs();
            uploadWithOption(zipName + ".zip",destinationPath + "/" + zipName + ".zip", parameters, metadata);
        }
        catch (NetworkErrorException | FileException e)
        {
            deleteDirectory(zipFile);
            deleteDirectory(new File(zipName + ".zip"));
            throw e;
        }
        File toErase = new File(zipName + ".zip");
        deleteDirectory(zipFile);
        toErase.delete();
        return;
    }

    @Override
    public void createFile(int num, String destinationPath, String fileName, ArrayList<String> parameters, ArrayList<HashMap<String,String> > metadataList) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, CreateFailedException, FolderException, FileException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isCreateFile())
            throw new PermissionException("Error, you dont have permission to create file");
        String saveDestinationPath = destinationPath;
        saveDestinationPath = saveDestinationPath.replace("\\", "/");
        if(saveDestinationPath.length()>0 && saveDestinationPath.charAt(0) == '/')
            throw new InvalidFormatException("Error, invalid destination path");
        if(saveDestinationPath.length()>0 && saveDestinationPath.charAt(saveDestinationPath.length()-1)=='/')
            saveDestinationPath = saveDestinationPath.substring(0,saveDestinationPath.length()-1);
        destinationPath = getAbsolutePathForPath(destinationPath);
        if (destinationPath.trim().length() == 0)
            throw new InvalidFormatException("Error, destination path not entered");
        if (fileName.trim().length() == 0)
            throw new InvalidFormatException("Error, file name not entered");
        if (destinationPath.charAt(destinationPath.length() - 1) == '/')
            throw new InvalidFormatException("Error, invalid file path");
        if(destinationPath.contains("."))
            throw new InvalidFormatException("Error, invalid file path");
        for (int i = 0; i < destinationPath.length() - 1; i++)
        {
            if (destinationPath.charAt(i) == destinationPath.charAt(i + 1) && destinationPath.charAt(i) == '/')
                throw new InvalidFormatException("Error, invalid file path");
        }
        if (fileName.contains("/") || fileName.contains("\\"))
            throw new InvalidFormatException("Error, invalid file name");
        if(!fileName.contains("."))
            throw new InvalidFormatException("Error, file doesn't have extension");
        ArrayList<String> list = getForbiddenExtensions();
        String splt[] = fileName.split("\\.");
        if(splt.length!=2)
            throw new InvalidFormatException("Error, invalid file name");
        if(splt[1].equals("storage"))
            throw new CreateFailedException("Error, .storage files cannot be created");
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).equals(splt[1]))
                throw new CreateFailedException("Error, " + splt[1] + " file extension forbidden");
        }
        String fullPathAndName = saveDestinationPath + "/" + fileName;
        File fileTmp = new File(destinationPath);
        if(!fileTmp.exists())
        {
            if (parameters != null && !parameters.contains("-c"))
                throw new FolderException("Error, some folders from destination path are missing");
            if(!fileTmp.mkdirs())
                throw new FolderException("Error while creating missing folders");
        }
        if(num == 0)
        {
            File file = new File(fileName);
            try
            {
                file.createNewFile();
                try
                {
                    uploadWithOption(fileName,fullPathAndName,parameters,((metadataList==null || metadataList.size()==0) ? null : metadataList.get(0)));
                }
                catch (NetworkErrorException | FileException e)
                {
                    file.delete();
                    throw e;
                }
                file.delete();
                return;
            }
            catch (IOException e)
            {
                throw new FileException("Error while creating file");
            }
        }
        File file = new File(fileName);
        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            throw new FileException("Error while creating file");
        }
        String split[] = fileName.split("\\.");
        boolean ex = false;
        String s = "";
        for(int i=1;i<=num;i++)
        {
            try
            {
                uploadWithOption(fileName,saveDestinationPath + "/" + split[0] + i + "." + split[1],parameters,((metadataList==null  || metadataList.size()==0) ? null : metadataList.get(i-1)));
            }
            catch (NetworkErrorException | FileException e)
            {
                ex = true;
                s += split[0] + i + "." + split[1] + " : " + e.getMessage() + "\n";
            }
        }
        file.delete();
        if(ex)
            throw new FileException(s.substring(0,s.length()-1));
        return;
    }

    @Override
    public void uploadFile(int num, String destinationPath, ArrayList<String> userPCFilePaths, ArrayList<String> fileNames, ArrayList<String> parameters, ArrayList<HashMap<String,String>> metadataList) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, UploadFailedException, FolderException, FileException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isCreateFile())
            throw new PermissionException("Error, you dont have permission to upload file");
        if(num == 0)
        {
            uploadFile(destinationPath, userPCFilePaths.get(0), fileNames.get(0),parameters,((metadataList==null || metadataList.size()==0) ? null : metadataList.get(0)));
            return;
        }
        boolean ex = false;
        String s = "";
        for(int i=1;i<=num;i++)
        {
            try
            {
                uploadFile(destinationPath, userPCFilePaths.get(i-1),fileNames.get(i-1),parameters,((metadataList==null || metadataList.size()==0) ? null : metadataList.get(i-1)));
            }
            catch (InvalidFormatException | NetworkErrorException | UploadFailedException | FolderException | FileException e)
            {
                ex = true;
                s += fileNames.get(i-1)+ " : " + e.getMessage() + "\n";
            }
        }
        if(ex)
            throw new FileException(s.substring(0,s.length()-1));
        return;
    }

    @Override
    public void deleteFolder(String folderPath, String folderName) throws InvalidFormatException, UserException, PermissionException, FolderException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isDeleteFolder())
            throw new PermissionException("Error, you dont have permission to delete folder");
        folderPath = getAbsolutePathForPath(folderPath);
        if (folderPath.trim().length() == 0)
            throw new InvalidFormatException("Error, folder path not entered");
        if (folderName.trim().length() == 0)
            throw new InvalidFormatException("Error, folder name not entered");
        if (folderPath.contains(".") || folderPath.charAt(folderPath.length() - 1) == '/')
            throw new InvalidFormatException("Error, invalid folder path");
        for (int i = 0; i < folderPath.length() - 1; i++)
        {
            if (folderPath.charAt(i) == folderPath.charAt(i + 1) && folderPath.charAt(i) == '/')
                throw new InvalidFormatException("Error, invalid folder path");
        }
        if (folderName.contains("/") || folderName.contains("\\") || folderName.contains("."))
            throw new InvalidFormatException("Error, invalid folder name");
        if(delete(folderPath + "/" + folderName))
            return;
        throw new FolderException("Error, folder doesn't exist");
    }

    @Override
    public void deleteFile(String filePath, String fileName) throws InvalidFormatException, UserException, PermissionException, DeleteFailedException, FileException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isDeleteFile())
            throw new PermissionException("Error, you dont have permission to delete file");
        String saveFilePath = filePath;
        saveFilePath = saveFilePath.replace("\\", "/");
        if(saveFilePath.length()>0 && saveFilePath.charAt(0) == '/')
            throw new InvalidFormatException("Error, invalid file path");
        filePath = getAbsolutePathForPath(filePath);
        if (filePath.trim().length() == 0)
            throw new InvalidFormatException("Error, file path not entered");
        if (fileName.trim().length() == 0)
            throw new InvalidFormatException("Error, file name not entered");
        if (filePath.charAt(filePath.length() - 1) == '/')
            throw new InvalidFormatException("Error, invalid file path");
        if(filePath.contains("."))
            throw new InvalidFormatException("Error, invalid file path");
        for (int i = 0; i < filePath.length() - 1; i++)
        {
            if (filePath.charAt(i) == filePath.charAt(i + 1) && filePath.charAt(i) == '/')
                throw new InvalidFormatException("Error, invalid file path");
        }
        if (fileName.contains("/") || fileName.contains("\\") || !fileName.contains("."))
            throw new InvalidFormatException("Error, invalid file name");
        if(fileName.contains(".storage"))
            throw new DeleteFailedException("Error, .storage files cannot be deleted");
        if(delete(filePath + "/" + fileName))
        {
            String newFileName = "";
            newFileName += "metadata";
            for(int i=0;i<fileName.length();i++)
            {
                if(fileName.charAt(i)=='.')
                    newFileName+="_";
                else
                    newFileName += fileName.charAt(i);
            }
            newFileName += ".json";
            delete(filePath + "/" + newFileName);
            return;
        }
        throw new FileException("Error, file doesn't exist");
    }

    @Override
    public void forbidExtension(String ext) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, FileException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().isAdmin())
            throw new PermissionException("Error, you dont have permission to forbid extensions");
        if(ext.equals("storage") || ext.equals("json"))
            throw new InvalidFormatException("Error, " + ext + " extension cannot be forbidden");
        if (!download("forbiddenExtensions.storage", "forbiddenExtensions.storage"))
            throw new NetworkErrorException("Error while connecting to database");
        File forbiddenExtensions = new File("forbiddenExtensions.storage");
        Scanner sc = null;
        try
        {
            sc = new Scanner(forbiddenExtensions);
            while (sc.hasNextLine())
            {
                String curr = sc.nextLine();
                if(curr.equals(ext))
                {
                    sc.close();
                    forbiddenExtensions.delete();
                    return;
                }
            }
            sc.close();
        }
        catch (FileNotFoundException e)
        {
            sc.close();
            forbiddenExtensions.delete();
            throw new FileException("Error while forbidding exception");
        }
        try
        {
            boolean success;
            FileWriter fw = new FileWriter(forbiddenExtensions, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(ext);
            pw.close();
            fw.close();
            success = upload("forbiddenExtensions.storage", "forbiddenExtensions.storage");
            if(!success)
            {
                forbiddenExtensions.delete();
                throw new NetworkErrorException("Error while connecting to database");
            }
            forbiddenExtensions.delete();
            return;
        }
        catch (IOException e)
        {
            forbiddenExtensions.delete();
            throw new NetworkErrorException("Error while connecting to database");
        }
    }

    @Override
    public void downloadFile(String remotePath, String localPath, ArrayList<String> parameters) throws InvalidFormatException,  UserException, PermissionException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isDownload())
            throw new PermissionException("Error, you dont have permission to download file");
        remotePath = remotePath.replace("\\","/");
        String split[] = remotePath.split("/");
        String saveOldLocalPath = localPath;
        if(split.length<1)
            throw new InvalidFormatException("Error, invalid path entered");
        localPath += File.separator + split[split.length-1];
        String jsonName = "";
        String newRemotePath = "";
        for(int i=0;i<split.length-1;i++)
        {
            newRemotePath += split[i];
            newRemotePath += "/";
        }
        jsonName += "metadata";
        for(int i=0;i<split[split.length-1].length();i++)
        {
            if(split[split.length-1].charAt(i)=='.')
                jsonName+="_";
            else
                jsonName += split[split.length-1].charAt(i);
        }
        jsonName += ".json";
        if(!new File(getAbsolutePathForPath(remotePath)).exists())
            return;
        download(remotePath, localPath);
        if(parameters.contains("-m"))
        {
            download(newRemotePath + jsonName, saveOldLocalPath + File.separator + jsonName);
        }
    }

    @Override
    public void downloadZip(String remotePath, String localPath) throws InvalidFormatException,  UserException, PermissionException
    {
        if (!User.isCreated())
            throw new UserException("Error, nobody logged in");
        if (!User.getInstance().getPermission().isDownload())
            throw new PermissionException("Error, you dont have permission to download zip");
        remotePath = remotePath.replace("\\","/");
        String split[] = remotePath.split("/");
        if(split.length<1)
            throw new InvalidFormatException("Error, invalid path entered");
        localPath += File.separator + split[split.length-1] + ".zip";
        if(!new File(getAbsolutePathForPath(remotePath)).exists())
            return;

        remotePath = getAbsolutePathForPath(remotePath);
        try
        {
            FolderZiper.zipFolder(remotePath,localPath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> storageContent(String s)
    {
        List<String> list = new ArrayList<>();
        listEverything(list,s);
        return list;
    }

    private void listEverything(List<String> lista, String remotePath)
    {
        File data = new File(remotePath);
        if (data.isDirectory()){
            lista.add(remotePath);
            File[] files = data.listFiles();
            for (File file : files)
                listEverything(lista,file.getPath());
        }else {
            lista.add(remotePath);
        }
    }

    private boolean create(String fullPathAndName, ArrayList<String> parameters)
    {
        File file = new File(fullPathAndName);
        try
        {
            if(!file.mkdir())
            {
                if (parameters != null && parameters.contains("-o"))
                {
                    delete(fullPathAndName);
                    create(fullPathAndName,parameters);
                    return true;
                }
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }

    }

    private boolean delete(String remotePath)
    {
        String name;
        if(remotePath.contains(ROOT_DIRECTORY_PATH) && remotePath.startsWith(ROOT_DIRECTORY_PATH))
            name = remotePath;
        else
            name = ROOT_DIRECTORY_PATH + File.separator + remotePath;
        File file = new File(name);
        if(file.exists())
        {
            if(file.isDirectory())
               return deleteDirectory(file);
            return file.delete();
        }
        return true;
    }

    private boolean upload(String localPath, String remotePath)
    {
        String name;
        if(remotePath.contains(ROOT_DIRECTORY_PATH) && remotePath.startsWith(ROOT_DIRECTORY_PATH))
            name = remotePath;
        else
            name = ROOT_DIRECTORY_PATH + File.separator + remotePath;
        try
        {
            Files.copy(new File(localPath).toPath(), new File(name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    private boolean download(String remotePath, String localPath)
    {
        String name;
        if(remotePath.contains(ROOT_DIRECTORY_PATH) && remotePath.startsWith(ROOT_DIRECTORY_PATH))
            name = remotePath;
        else
            name = ROOT_DIRECTORY_PATH + File.separator + remotePath;
        try
        {
            Files.copy(new File(name).toPath(), new File(localPath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            return false;
        }
        return true;
    }

    private boolean deleteDirectory(File directoryToBeDeleted)
    {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private void checkUser(String username, String password) throws InvalidFormatException
    {
        if (username.equals(""))
            throw new InvalidFormatException("Error, username not entered");
        if (password.equals(""))
            throw new InvalidFormatException("Error, password not entered");
        if (username.contains(" "))
            throw new InvalidFormatException("Error, username cannot contain spaces");
        if (username.contains("/"))
            throw new InvalidFormatException("Error, username cannot contain /");
        return;
    }

    private String hashPassword(String pass, String lvl)
    {
        String generatedPassword = "";
        try
        {
            StringBuilder sb = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance(lvl);
            byte[] byteArray = md.digest(pass.getBytes());
            for (int i = 0; i < byteArray.length; i++)
            {
                sb.append(Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    private boolean writePermissionForUser(String username, boolean admin, File file)
    {
        try
        {
            String user = username + "///";
            for (int i = 0; i < Permission.permissionCount - 1; i++)
                user += (admin ? "true," : "false,");
            user += (admin ? "true" : "false");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(user);
            pw.close();
            fw.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Permission getPermissionForUser(String username, File userPermissions) throws UserException
    {
        Permission permission = new Permission();
        Scanner sc;
        try
        {
            sc = new Scanner(userPermissions);
            while (sc.hasNextLine())
            {
                String curr = sc.nextLine();
                String[] split = curr.split("///");
                if (split[0].equals(username))
                {
                    ArrayList<Boolean> permissions = new ArrayList<>();
                    String[] splitPer = split[1].split(",");
                    for (String str : splitPer)
                    {
                        if (str.equals("true"))
                            permissions.add(true);
                        else if (str.equals("false"))
                            permissions.add(false);
                    }
                    permission.setPermissions(permissions);
                    sc.close();
                    return permission;
                }
            }
            sc.close();
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }
        throw new UserException("User doesn't exist in database");
    }

    private boolean replaceLine(File file, String oldLine, String newLine)
    {
        try
        {
            List<String> fileContent = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
            for (int i = 0; i < fileContent.size(); i++)
            {
                if (fileContent.get(i).equals(oldLine))
                {
                    fileContent.set(i, newLine);
                    Files.write(file.toPath(), fileContent, StandardCharsets.UTF_8);
                    return true;
                }
            }
        }
        catch (IOException err)
        {
            err.printStackTrace();
        }
        return false;
    }

    private String getAbsolutePathForPath(String path)
    {
        if(path.length() == 0)
            return ROOT_DIRECTORY_PATH;
        path = path.replace("\\", "/");
        String finalPath = ROOT_DIRECTORY_PATH;
        if(path.charAt(0) != '/')
            path = "/" + path;
        if(path.charAt(path.length()-1)=='/')
            path = path.substring(0,path.length()-1);
        finalPath+=path;
        finalPath = finalPath.replace("/", File.separator);
        return finalPath;
    }

    private void uploadWithOption(String localPath, String remotePath, ArrayList<String> parameters, HashMap<String,String> metadata) throws NetworkErrorException, FileException
    {
        String splt[] = remotePath.split("/");
        String flName = splt[splt.length-1];
        String saveRemotePath = remotePath;
        remotePath = getAbsolutePathForPath(remotePath);
        String newRemotePath="";
        saveRemotePath = saveRemotePath.replace("\\", "/");
        String split[] = saveRemotePath.split("/");
        newRemotePath = "";
        for(int i=0;i<split.length-1;i++)
        {
            newRemotePath += split[i];
            newRemotePath += File.separator;
        }
        newRemotePath += "metadata";
        for(int i=0;i<split[split.length-1].length();i++)
        {
            if(split[split.length-1].charAt(i)=='.')
                newRemotePath+="_";
            else
                newRemotePath += split[split.length-1].charAt(i);
        }
        newRemotePath += ".json";
        if(new File(remotePath).exists())
        {
            if (!parameters.contains("-o"))
                throw new FileException("Error, file already exists");
        }
        delete(getAbsolutePathForPath(newRemotePath));
        File file = new File(remotePath);
        if(!file.exists())
        {
            try
            {
                if(!file.createNewFile())
                    throw new FileException("Error while working with file");
            }
            catch (IOException e)
            {
                //e.printStackTrace();
                throw new FileException("Error while working with file");
            }
        }
        upload(localPath,remotePath);
        if(!parameters.contains("-m"))
            return;
        uploadMetadata(newRemotePath,metadata);
    }

    private void uploadMetadata(String newRemotePath, HashMap<String,String> metadata) throws NetworkErrorException
    {
        try
        {
            HashMap<String,String> hashMap = metadata;
            Gson gsonObject = new GsonBuilder().setPrettyPrinting().create();
            String JSONObject = gsonObject.toJson(hashMap);
            File jsonFile = new File("tmp.json");
            jsonFile.createNewFile();
            FileWriter fw = new FileWriter(jsonFile, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(JSONObject);
            pw.close();
            fw.close();
            boolean success = upload("tmp.json", newRemotePath);
            if(!success)
            {
                jsonFile.delete();
                throw new NetworkErrorException("Error while accessing storage database");
            }
            jsonFile.delete();
            return;
        }
        catch (IOException e)
        {
            throw new NetworkErrorException("Error while accessing storage database");
        }
    }

    private ArrayList<String> getForbiddenExtensions() throws NetworkErrorException
    {
        ArrayList<String> list = new ArrayList<>();
        if (!download("forbiddenExtensions.storage", "forbiddenExtensions.storage"))
            throw new NetworkErrorException("Error while accessing storage database");
        File forbiddenExtensions = new File("forbiddenExtensions.storage");
        Scanner sc = null;
        try
        {
            sc = new Scanner(forbiddenExtensions);
            while(sc.hasNextLine())
            {
                String curr = sc.nextLine();
                curr = curr.trim();
                if(curr.length()>0)
                    list.add(curr);
            }
            sc.close();
            forbiddenExtensions.delete();
            return list;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            sc.close();
            forbiddenExtensions.delete();
            throw new NetworkErrorException("Error while accessing storage database");
        }
    }

    private void uploadFile(String destinationPath, String userPCfilePath, String fileName, ArrayList<String> parameters, HashMap<String, String> metadata) throws InvalidFormatException, NetworkErrorException, UploadFailedException, FolderException, FileException
    {
        Scanner sc;
        String split1[] = userPCfilePath.split("\\.");
        String split2[] = fileName.split("\\.");
        if(!split1[split1.length-1].equals(split2[split2.length-1]))
            throw new FileException("Error, file extensions are not the same");
        File fileToUpload = new File(userPCfilePath);
        if(!fileToUpload.exists())
            throw new FileException("Error, file doesn't exits");
        if(fileToUpload.isDirectory())
            throw new FileException("Error, entered directory instead of file");
        String saveDestinationPath = destinationPath;
        saveDestinationPath = saveDestinationPath.replace("\\", "/");
        if(saveDestinationPath.length()>0 && saveDestinationPath.charAt(0) == '/')
            throw new InvalidFormatException("Error, invalid destionation path");
        if(saveDestinationPath.length()>0 && saveDestinationPath.charAt(saveDestinationPath.length()-1)=='/')
            saveDestinationPath = saveDestinationPath.substring(0,saveDestinationPath.length()-1);
        destinationPath = getAbsolutePathForPath(destinationPath);
        if (destinationPath.trim().length() == 0)
            throw new InvalidFormatException("Error, destionation path not entered");
        if (fileName.trim().length() == 0)
            throw new InvalidFormatException("Error, file name not entered");
        if (destinationPath.charAt(destinationPath.length() - 1) == '/')
            throw new InvalidFormatException("Error, invalid file path");
        if(destinationPath.contains("."))
            throw new InvalidFormatException("Error, invalid file path");
        for (int i = 0; i < destinationPath.length() - 1; i++)
        {
            if (destinationPath.charAt(i) == destinationPath.charAt(i + 1) && destinationPath.charAt(i) == '/')
                throw new InvalidFormatException("Error, invalid file path");
        }
        if (fileName.contains("/") || fileName.contains("\\"))
            throw new InvalidFormatException("Error, invalid file name");
        if(!fileName.contains("."))
            throw new FileException("Error, file extension missing");
        ArrayList<String> list = getForbiddenExtensions();
        String splt[] = fileName.split("\\.");
        if(splt.length!=2)
            throw new InvalidFormatException("Error, invalid file name");
        if(splt[1].equals("storage"))
            throw new UploadFailedException("Error, file with .storage extension cannot be uploaded");
        for(int i=0;i<list.size();i++)
        {
            if(list.get(i).equals(splt[1]))
                throw new InvalidFormatException("Error, files with " + splt[1] + " extension (forbidden) cannot be uploaded");
        }
        String fullPathAndName = saveDestinationPath + "/" + fileName;
        File fileTmp = new File(destinationPath);
        if(!fileTmp.exists())
        {
            if (parameters != null && !parameters.contains("-c"))
                throw new FolderException("Error, some folders from destination path are missing");
            if(!fileTmp.mkdirs())
                throw new FolderException("Error while creating missing folders");
        }
        uploadWithOption(userPCfilePath,fullPathAndName,parameters, metadata);
    }
}
