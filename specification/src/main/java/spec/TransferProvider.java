package spec;

import exceptions.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TransferProvider
{
    boolean initStorage(String remotePath) throws InitStorageException;

    void registerAdmin(String username, String password) throws InvalidFormatException, NetworkErrorException, TerminalErrorException;

    void login(String username, String password) throws InvalidFormatException, NetworkErrorException, UserException, LoginException;

    void logout() throws LogoutException;

    void register(String username, String password) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, TerminalErrorException, RegisterException;

    void changePassword(String username, String oldPassword, String newPassword) throws InvalidFormatException, NetworkErrorException, UserException;

    void changePermission(String username, ArrayList<Boolean> changedPermissions, ArrayList<Integer> indexes) throws NetworkErrorException, UserException, PermissionException;

    void createFolder(int numberOfFolders,String destinationPath, String folderName, ArrayList<String> parameters) throws InvalidFormatException, UserException, PermissionException, FolderException;

    void uploadZip(int numberOfFiles, String zipName, ArrayList<String> userPCfilePaths, String destinationPath, ArrayList<String> parameters, HashMap<String,String> metadata) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, FolderException, FileException;

    void createFile(int numberOfFiles, String destinationPath, String fileName, ArrayList<String> parameters, ArrayList<HashMap<String,String> > metadataList) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, CreateFailedException, FolderException, FileException;

    void uploadFile(int numberOfFiles, String destinationPath, ArrayList<String> userPCFilePaths, ArrayList<String> fileNames, ArrayList<String> parameters, ArrayList<HashMap<String,String>> metadataList) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, UploadFailedException, FolderException, FileException;

    void deleteFolder(String folderPath, String folderName) throws InvalidFormatException, UserException, PermissionException, FolderException;

    void deleteFile(String filePath, String fileName) throws InvalidFormatException, UserException, PermissionException, DeleteFailedException, FileException;

    void forbidExtension(String extension) throws InvalidFormatException, NetworkErrorException, UserException, PermissionException, FileException;

    void downloadFile(String remotePath, String localPath, ArrayList<String> parameters) throws InvalidFormatException,  UserException, PermissionException;

    void downloadZip(String remotePath, String localPath) throws InvalidFormatException,  UserException, PermissionException;

    List<String> storageContent(String remotePath);


}
