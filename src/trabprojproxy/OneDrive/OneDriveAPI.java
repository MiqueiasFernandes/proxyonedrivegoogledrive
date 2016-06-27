/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy.OneDrive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.StringUtils;
import trabprojproxy.OneDrive.enums.FriendlyNamesEnum;
import trabprojproxy.OneDrive.enums.OneDriveEnum;
import trabprojproxy.OneDrive.model.Credenciais;
import trabprojproxy.OneDrive.model.OAuth20Token;
import trabprojproxy.OneDrive.model.Quota;
import trabprojproxy.OneDrive.model.User;
import trabprojproxy.OneDrive.model.folder.Data;
import trabprojproxy.OneDrive.model.folder.File;
import trabprojproxy.OneDrive.model.folder.Folder;
import trabprojproxy.OneDrive.model.folder.SharedLink;

/**
 *
 * @author mfernandes
 */
public class OneDriveAPI {

    private static final String API_PATH_ME = "me";
    private static final String API_PATH_ME_SKYDRIVE_QUOTA = "me/skydrive/quota";
    private static final String API_PATH_ME_SKYDRIVE = "me/skydrive";
    private static final String API_PATH_ME_SKYDRIVE_FILES = "me/skydrive/files";
    private static final String API_PATH_FILES = "files";
    private static final String API_PATH_CONTENT = "content?download=true";
    private static final String API_PATH_SHARED_EDIT_LINK = "shared_edit_link";
    private static final String API_PATH_SHARED_READ_LINK = "shared_read_link";

    private REST rest;
    private Credenciais credenciais;

    public OneDriveAPI(Credenciais credenciais) throws IOException, Exception {
        rest = new REST();
        this.credenciais = credenciais;
        if (credenciais.getoAuth20Token() == null) {
            initAccessTokenByRefreshTokenAndClientId();
        }
        System.out.println("credenciais preparadas em ONEDRIVEAPI : \n" + credenciais);
    }

    public String getFileID(String name) throws Exception {
        Folder folder = getMyFilesList(FriendlyNamesEnum.ALL);

        for (Data data : folder.getData()) {
            System.out.println("Entry name: " + data.getName() + ", type: " + data.getType() + ", id: " + data.getId());
            if (name.equals(data.getName())) {
                return data.getId();
            }
        }
        return null;
    }

    public String leConteudoDoArquivo(String nome) throws Exception {

        String fileID = getFileID(nome);

        InputStream openFile = openFile(fileID);

        InputStreamReader ir = new InputStreamReader(openFile);

        BufferedReader br = new BufferedReader(ir);

        String line, texto = "";

        while ((line = br.readLine()) != null) {
            texto += line;
        }

        return texto;
    }

    public boolean gravaArquivo(java.io.File arquivo) throws Exception {

        String fileID = getFileID(arquivo.getName());

        if (fileID == null) {
            return uploadFile(arquivo, "") != null;
        } else {
            File file = getFile(fileID);
            deleteFile(file);
            return uploadFile(arquivo, "") != null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public User getUser() throws Exception {
        return (User) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, API_PATH_ME, new User());
    }

    /**
     * {@inheritDoc}
     */
    public Quota getQuota() throws Exception {
        return (Quota) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, API_PATH_ME_SKYDRIVE_QUOTA, new Quota());
    }

    /**
     * {@inheritDoc}
     */
    public Folder getMyFilesList(FriendlyNamesEnum friendlyNamesEnum) throws Exception {
        String apiPath = API_PATH_ME_SKYDRIVE;

        if (friendlyNamesEnum.equals(FriendlyNamesEnum.ALL)) {
            apiPath += "/" + API_PATH_FILES;
        } else {
            apiPath += "/" + friendlyNamesEnum.toString() + "/" + API_PATH_FILES;
        }

        return (Folder) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, new Folder());
    }

    /**
     * {@inheritDoc}
     */
    public Folder getFileList(String folderId) throws Exception {
        return (Folder) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, folderId + "/" + API_PATH_FILES, new Folder());
    }

    /**
     * {@inheritDoc}
     */
    public void downloadFile(File oneDriveFile, String destinationFilePath) {
        ClientResponse clientResponse = rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_OCTET_STREAM, oneDriveFile.getId() + "/" + API_PATH_CONTENT);
        java.io.File destinationFile = new java.io.File(destinationFilePath);

        if (clientResponse.getStatus() == ClientResponse.Status.FOUND.getStatusCode()) {
            /* The response is a redirect location, so do a new call to get the real download location */
            try {
                clientResponse = rest.doGetAPI(clientResponse.getLocation().toString(), MediaType.APPLICATION_OCTET_STREAM);
                java.io.File resultFile = clientResponse.getEntity(java.io.File.class);
                org.apache.commons.io.FileUtils.moveFile(resultFile, destinationFile);
                System.out.println("OneDriveAPI file '" + oneDriveFile.getName() + "' saved to '" + destinationFilePath + "' (" + destinationFile.length() + " bytes)");
            } catch (Exception exception) {
                System.err.println("Could not download from redirect location " + clientResponse.getLocation());
                exception.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            /* The response contains the location, so save the response to a file */
            try {
                java.io.File resultFile = clientResponse.getEntity(java.io.File.class);
                org.apache.commons.io.FileUtils.moveFile(resultFile, destinationFile);
                System.out.println("OneDriveAPI file '" + oneDriveFile.getName() + "' saved to '" + destinationFilePath + "' (" + destinationFile.length() + " bytes)");
            } catch (Exception e) {
                System.err.println("Cannot download or write file with identifier '" + oneDriveFile.getId() + "' to destination '" + destinationFilePath + "'");
                e.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public InputStream openFile(String fileId) throws IOException, Exception {
        ClientResponse clientResponse = rest.
                doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_OCTET_STREAM, fileId + "/" + API_PATH_CONTENT);

        if (clientResponse.getStatus() == ClientResponse.Status.FOUND.getStatusCode()) {
            /* The response is a redirect location, so do a new call to get the real download location */
            try {
                clientResponse = rest.doGetAPI(
                        clientResponse.getLocation().toString(), MediaType.APPLICATION_OCTET_STREAM
                );
                return clientResponse.getEntity(InputStream.class);
            } catch (Exception e) {
                System.err.println("Could not open file from redirect location " + clientResponse.getLocation());
                e.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            /* The response contains the location, so save the response to a file */
            try {
                return clientResponse.getEntity(InputStream.class);
            } catch (Exception e) {
                System.err.println("Cannot open file with identifier '" + fileId + "'");
                e.printStackTrace();
            }
        } else if (clientResponse.getStatus() == ClientResponse.Status.UNAUTHORIZED.getStatusCode()) {
            /* Update access token and try open file again */
            initAccessTokenByRefreshTokenAndClientId();
            return openFile(fileId);
        }
        throw new RuntimeException("Cannot download file with identifier '" + fileId + "'");
    }

    /**
     * {@inheritDoc}
     */
    public File getFile(String fileId) throws Exception {
        return (File) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, fileId, new File());
    }

    /**
     * {@inheritDoc}
     */
    public Folder getFolder(String folderId) throws Exception {
        return (Folder) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, folderId, new Folder());
    }

    /**
     * {@inheritDoc}
     */
    public void deleteFile(File oneDriveFile) throws Exception {
        rest.doDeleteAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFile.getId());
    }

    /**
     * {@inheritDoc}
     */
    public File updateFile(File oneDriveFile) throws Exception, IOException {
        return (File) rest.doPutAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFile.getId(), oneDriveFile);
    }

    /**
     * {@inheritDoc}
     */
    public Folder updateFolder(Folder oneDriveFolder) throws Exception, IOException {
        return (Folder) rest.doPutAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, oneDriveFolder.getId(), oneDriveFolder);
    }

    /**
     * {@inheritDoc}
     */
    public Folder createFolder(String name, String description, String locationFolderId) throws Exception, IOException {
        String apiPath;
        Folder oneDriveFolder = new Folder();
        oneDriveFolder.setName(name);
        oneDriveFolder.setDescription(description);

        if (StringUtils.isEmpty(locationFolderId)) {
            apiPath = API_PATH_ME_SKYDRIVE;
        } else {
            apiPath = locationFolderId;
        }

        return (Folder) rest.doPostAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, oneDriveFolder);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteFolder(String folderId) throws Exception {
        rest.doDeleteAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, folderId);
    }

    /**
     * Send a file to OneDriveAPI by uploading the content. If no folder
     * identifier is given the file will be put in the personal folder.<br>
     * If the folder identifier is given the file will be put in the specified
     * folder identifier location.
     *
     * @param file file to upload to OneDriveAPI
     * @param folderId folder identifier to put the file into
     * @return OneDriveAPI file object
     */
    public File uploadFile(java.io.File file, String folderId) {
        File oneDriveFile = new File();
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        ObjectMapper objectMapper = new ObjectMapper();
        String apiPath = "";

        if (folderId.isEmpty()) {
            apiPath = API_PATH_ME_SKYDRIVE_FILES + "/" + file.getName();
        } else {
            apiPath = folderId + "/" + API_PATH_FILES + "/" + file.getName();
        }

        queryParams.add(rest.API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        WebResource webResource = rest.getClient().resource(OneDriveEnum.API_URL.toString() + apiPath);

        try {
            ClientResponse clientResponse = webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, fileToByteArray(file));
            oneDriveFile = objectMapper.readValue(clientResponse.getEntity(String.class).toString(), File.class);
        } catch (Exception e) {
            System.err.println("Cannot upload file '" + file.getAbsolutePath() + "' to OneDriveAPI");
            e.printStackTrace();
        }

        return oneDriveFile;
    }

    /**
     * Convert file content to a byte array.
     *
     * @param file file object to convert to a byte array
     * @return byte array holding the file content
     */
    private byte[] fileToByteArray(java.io.File file) {
        byte[] byteArrayFile = new byte[(int) file.length()];

        try {
            FileInputStream fileInputStream = null;
            fileInputStream = new FileInputStream(file);
            fileInputStream.read(byteArrayFile);
            fileInputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return byteArrayFile;
    }

    /**
     * {@inheritDoc}
     */
    public SharedLink getSharedLink(String fileId, boolean isEditable) throws Exception {
        String apiPath;

        if (isEditable) {
            apiPath = fileId + "/" + API_PATH_SHARED_EDIT_LINK;
        } else {
            apiPath = fileId + "/" + API_PATH_SHARED_READ_LINK;
        }

        return (SharedLink) rest.doGetAPI(new MultivaluedMapImpl(), MediaType.APPLICATION_JSON, apiPath, new SharedLink());
    }

    private void initAccessTokenByRefreshTokenAndClientId() throws IOException, Exception {
        OAuth20Token tokenByCredencial = rest.getTokenByCredencial(credenciais);
        credenciais.setoAuth20Token(tokenByCredencial);
    }

}
