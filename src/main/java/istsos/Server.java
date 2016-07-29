package istsos;

import istsos.Requests.Request;

import java.util.*;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Realm.AuthScheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Server class.
 * <p>
 * Enables services for IstSOS environment.
 *
 */
public class Server{
	
	private String name;
	private String url;

	private String user;
	private String password;
	private boolean autheticationRequired = false;
	

	private List<Service> services = new ArrayList<>();

	private AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
	
	public Server(String name, String url) {
		this.name = name;
		this.url = url;
	}
	
	public Server(String serverName, String url, String user, String password) {
		this.name = serverName;
		this.url = url;
		this.setUser(user);
		this.setPassword(password);
		autheticationRequired = true;
	}


	public Service getService(String serviceName){

		for(Service service : this.services){

			if (service.getName().equals(serviceName))
				return service;
		}

		return null;
	}
	
	public String getServerName(){
		return this.name;
	}

	public String getServerUrl(){
		return this.url;
	}
	
	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void loadServices(){
		this.loadServices(null);
	}
	
	public void loadServices(IstSOSListener callback){
		services.clear();
		
		Map<String, String> urlKeyMap = new HashMap<String, String>();
		urlKeyMap.put("url", this.url);
		
		IstSOS.executeGet(Requests.getUrl(Request.SERVICE, urlKeyMap), new IstSOSListener() {
			
			@Override
			public void onSuccess(EventObject event) {
				
				JsonObject json = (JsonObject) event.getObject();
		        
		        JsonArray data = json.getAsJsonArray("data");
		      
                for(JsonElement element : data){
		        	if(element.isJsonObject()){
		        		JsonObject object = element.getAsJsonObject();
		        		
		        		System.out.println(object.toString());
		        		
		        		Service service = Service.fromJson(object);
		        		service.setServer(Server.this);
		        		
		        		Server.this.services.add(service);
		        		
		        	}
		        }
		        
		        EventObject eventObject = new EventObject(Event.SERVICE_LOADED, services);
	    		
	    		if(callback != null){
	    			callback.onSuccess(eventObject);
	    		}
			}
			
			@Override
			public void onError(EventObject event) {
				// TODO Auto-generated method stub
				
			}
		}, this.getRealm());
	}
	
	public List<Service> getServices() {
		return this.services;
	}
	
	protected Realm getRealm(){
		if(autheticationRequired){
    		return new Realm.Builder(this.getUser(), this.getPassword())
				.setUsePreemptiveAuth(true)
				.setScheme(AuthScheme.BASIC)
				.build();
    	}
		return null;
	}

    protected void executeGet(String url, AsyncCompletionHandler<Integer> onComplete){
    	BoundRequestBuilder builder = asyncHttpClient.prepareGet(url);
    	if(autheticationRequired){
    		Realm realm = new Realm.Builder(this.getUser(), this.getPassword())
				.setUsePreemptiveAuth(true)
				.setScheme(AuthScheme.BASIC)
				.build();
    		builder.setRealm(realm);
    	}
    	builder.execute(onComplete);
    	
    }
	
}