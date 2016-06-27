/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trabprojproxy.OneDrive;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import java.io.IOException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import trabprojproxy.OneDrive.enums.HttpOperationEnum;
import static trabprojproxy.OneDrive.enums.HttpOperationEnum.DELETE;
import static trabprojproxy.OneDrive.enums.HttpOperationEnum.GET;
import static trabprojproxy.OneDrive.enums.HttpOperationEnum.POST;
import static trabprojproxy.OneDrive.enums.HttpOperationEnum.PUT;
import trabprojproxy.OneDrive.enums.OneDriveEnum;
import trabprojproxy.OneDrive.model.Credenciais;
import trabprojproxy.OneDrive.model.OAuth20Token;

/**
 *
 * @author mfernandes
 */
public class REST {

    public static final String API_PARAM_ACCESS_TOKEN = "access_token";
    public static final String API_PARAM_CLIENT_ID = "client_id";
    public static final String API_PARAM_CLIENT_SECRET = "client_secret";
    public static final String API_PARAM_CODE = "code";
    public static final String API_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String API_PARAM_GRANT_TYPE = "grant_type";
    public static final String API_PARAM_REFRESH_TOKEN = "refresh_token";

    private Client client;

    private Credenciais credenciais;

    public REST() {

        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
        defaultClientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(defaultClientConfig);
    }

    public OAuth20Token getTokenByCredencial(Credenciais credenciais) throws IOException, Exception {
        OAuth20Token oAuth20Token = new OAuth20Token();
        WebResource webResource = client.resource(OneDriveEnum.OAUTH20_TOKEN_URL.toString());
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "client_id=" + credenciais.getClientId() + "&redirect_uri=https://login.live.com/oauth20_desktop.srf&code=" + credenciais.getAuthorizationCode() + "&grant_type=authorization_code";
        ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON).header("Content-Type", "application/x-www-form-urlencoded").header("Content-Length", String.valueOf(url.length())).entity(url).post(ClientResponse.class);
        try {
            oAuth20Token = objectMapper.readValue(clientResponse.getEntity(String.class).toString(), OAuth20Token.class);
            oAuth20Token.setStatus(clientResponse.getStatus());
        } catch (Exception e) {
            System.err.println("HOUVE UM ERRO ao tentar mapear resposta do cliente:");
            System.err.println(e);
        }

        if (oAuth20Token.getStatus() > 202) {
            System.err.println("PROVAVELMENTE HOUVE UM ERRO ENQUANTO GERAVA TOKEN");
            System.err.println("RESPOSTA DO SERVIDOR: " + oAuth20Token.getStatus());
            System.err.println("ERRO: " + oAuth20Token.getError());
            System.err.println("DESCRIÇÃO: " + oAuth20Token.getError_description());
            System.err.println("Se este erro esta relacionado a url incorreto veja as descrições abaixo:");
            credenciais.codigoIncorretoAlert();
            System.err.println("ABORTANDO CONEXÃO...");
            throw new Exception("SERVIDOR RECUSOU CONEXÃO: " + getGetError(oAuth20Token.getStatus()));
        }

        this.credenciais = credenciais;
        return oAuth20Token;
    }

    /**
     * This method is used for performing API calls.
     *
     * @param queryParams one or more parameters to be sent with the GET call
     * @param mediaType media type of the call
     * @param path path in the URL
     * @param resultObject object holding the response or result of the GET call
     * @return response result object
     */
    public Object doGetAPI(MultivaluedMap<String, String> queryParams, String mediaType, String path, Object resultObject) throws Exception {
        long startTime = System.currentTimeMillis();
        ObjectMapper objectMapper = new ObjectMapper();
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + path);

        queryParams.add(API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        ClientResponse clientResponse = webResource.queryParams(queryParams).accept(mediaType).get(ClientResponse.class);
        resultObject = objectMapper.readValue(clientResponse.getEntity(String.class).toString(), resultObject.getClass());

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Executed " + webResource.getURI() + " in " + elapsedTime + " ms");

        return resultObject;
    }

    /**
     * This method is used for performing API GET calls.
     *
     * @param queryParams one or more parameters to be send with the GET call
     * @param mediaType media type to accept
     * @param path path in the URL
     * @return client response object
     */
    public ClientResponse doGetAPI(MultivaluedMap<String, String> queryParams, String mediaType, String path) {
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + path);

        queryParams.add(API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        return webResource.queryParams(queryParams).accept(mediaType).get(ClientResponse.class);
    }

    /**
     * This method is used for doing native HTTP GET calls to any given url.
     *
     * @param url url to call with a GET method
     * @param mediaType media type to accept
     * @return client native response object
     */
    public ClientResponse doGetAPI(String url, String mediaType) {
        WebResource webResource = client.resource(url);

        return webResource.accept(mediaType).get(ClientResponse.class);
    }

    /**
     * This method is used for performing API DELETE calls.
     *
     * @param queryParams one or more parameters to be send with the DELETE call
     * @param mediaType media type to accept
     * @param path path in the URL
     */
    public void doDeleteAPI(MultivaluedMap<String, String> queryParams, String mediaType, String path) throws Exception {
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + path);

        queryParams.add(API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        ClientResponse clientResponse = webResource.queryParams(queryParams).accept(mediaType).delete(ClientResponse.class);

        if (clientResponse.getStatus() != ClientResponse.Status.NO_CONTENT.getStatusCode()) {
            throw new Exception(getHttpResponseError(HttpOperationEnum.DELETE, clientResponse.getStatus()));
        }
    }

    /**
     * This method is used for performing API POST calls.
     *
     * @param queryParams one or more parameters to be send with the POST call
     * @param mediaType media type to accept
     * @param path path in the URL
     * @param resultObject object holding the response or result of the POST
     * call
     * @return response result object
     */
    public Object doPostAPI(MultivaluedMap<String, String> queryParams, String mediaType, String path, Object resultObject) throws Exception, IOException {
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + path);
        ObjectMapper objectMapper = new ObjectMapper();

        queryParams.add(API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        ClientResponse clientResponse = webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(mediaType).post(ClientResponse.class, resultObject);

        if (clientResponse.getStatus() != ClientResponse.Status.CREATED.getStatusCode()) {
            throw new Exception(getHttpResponseError(HttpOperationEnum.POST, clientResponse.getStatus()));
        } else {
            return objectMapper.readValue(clientResponse.getEntity(String.class).toString(), resultObject.getClass());
        }
    }

    /**
     * This method is used for performing API PUT calls.
     *
     * @param queryParams one or more parameters to be send with the PUT call
     * @param mediaType media type to accept
     * @param path path in the URL
     * @param resultObject object holding the response or result of the POST
     * call
     * @return response result object
     */
    public Object doPutAPI(MultivaluedMap<String, String> queryParams, String mediaType, String path, Object resultObject) throws Exception, IOException {
        WebResource webResource = client.resource(OneDriveEnum.API_URL.toString() + path);
        ObjectMapper objectMapper = new ObjectMapper();

        queryParams.add(API_PARAM_ACCESS_TOKEN, credenciais.getoAuth20Token().getAccess_token());
        ClientResponse clientResponse = webResource.queryParams(queryParams).type(MediaType.APPLICATION_JSON).accept(mediaType).put(ClientResponse.class, resultObject);

        if (clientResponse.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
            throw new Exception(getHttpResponseError(HttpOperationEnum.PUT, clientResponse.getStatus()));
        } else {
            return objectMapper.readValue(clientResponse.getEntity(String.class).toString(), resultObject.getClass());
        }
    }

    /**
     * Get slightly more human readable response error information back.
     *
     * @param httpOperationEnum HTTP operation enumeration (POST, GET, PUT,
     * DELETE)
     * @param status HTTP response status
     * @return slightly more human readable response error information
     * @see HttpOperationEnum,ClientResponse.Status
     */
    private String getHttpResponseError(HttpOperationEnum httpOperationEnum, int status) {
        switch (httpOperationEnum) {
            case POST:
                return "POST: " + getPostError(status);
            case GET:
                return "GET: " + getGetError(status);
            case DELETE:
                return "DELETE: " + getDeleteError(status);
            case PUT:
                return "PUT: " + getPutError(status);
            default:
                return "Unknown HTTP operation";
        }
    }

    /**
     * Get the HTTP PUT status error.
     *
     * @param status HTTP response status
     * @return HTTP PUT status error
     */
    private String getPutError(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 202:
                return "Accepted";
            case 204:
                return "No content";
            case 400:
                return "Bad request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not found";
            case 405:
                return "Not allowed";
            case 409:
                return "Resource conflict";
            case 410:
                return "Gone";
            case 411:
                return "Length required";
            case 413:
                return "Request entity too large";
            case 414:
                return "Request URI too long";
            case 415:
                return "Unsupported type";
            case 500:
                return "Server error";
            case 501:
                return "Not implemented";
            case 502:
                return "Bad gateway";
            default:
                return "Unknown response code";
        }
    }

    /**
     * Get the HTTP DELETE status error.
     *
     * @param status HTTP response status
     * @return HTTP DELETE status error
     */
    private String getDeleteError(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 202:
                return "Accepted";
            case 204:
                return "No content";
            case 400:
                return "Bad request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not found";
            case 405:
                return "Not allowed";
            case 409:
                return "Resource conflict";
            case 501:
                return "Not implemented yet";
            case 502:
                return "Bad gateway";
            default:
                return "Unknown response code";
        }
    }

    /**
     * Get the HTTP GET status error.
     *
     * @param status HTTP response status
     * @return HTTP GET status error
     */
    private String getGetError(int status) {
        switch (status) {
            case 200:
                return "OK";
            case 204:
                return "No content";
            case 301:
                return "Moved permanently";
            case 303:
                return "See other";
            case 304:
                return "Not modified";
            case 400:
                return "Bad request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not found";
            case 405:
                return "Not allowed";
            case 406:
                return "Not acceptable";
            case 408:
                return "Request timeout";
            case 410:
                return "Gone";
            case 412:
                return "Precondition failed";
            case 416:
                return "Requested range not satisfiable";
            case 500:
                return "Server error";
            case 502:
                return "Bad gateway";
            case 505:
                return "HTTP version not supported";
            default:
                return "Unknown response code";
        }
    }

    /**
     * Get the HTTP POST status error.
     *
     * @param status HTTP response status
     * @return HTTP POST status error
     */
    private String getPostError(int status) {
        switch (status) {
            case 201:
                return "Created";
            case 202:
                return "Accepted";
            case 400:
                return "Bad request";
            case 401:
                return "Unauthorized";
            case 403:
                return "Forbidden";
            case 404:
                return "Not found";
            case 405:
                return "Not allowed";
            case 408:
                return "Request timeout";
            case 411:
                return "Length required";
            case 413:
                return "Request entity too large";
            case 414:
                return "Request URI too long";
            case 415:
                return "Unsupported type";
            case 500:
                return "Server error";
            case 501:
                return "Not implemented";
            case 502:
                return "Bad gateway";
            default:
                return "Unknown response code";
        }
    }

    public Client getClient() {
        return client;
    }

}
