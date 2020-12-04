package io.zbus.rpc.biz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.alibaba.fastjson.JSON;

import io.zbus.rpc.InvocationContext;
import io.zbus.rpc.annotation.Filter;
import io.zbus.rpc.annotation.Param;
import io.zbus.rpc.annotation.Route;
import io.zbus.rpc.biz.model.MyEnum;
import io.zbus.rpc.biz.model.Order;
import io.zbus.rpc.biz.model.User;
import io.zbus.transport.Message;
import io.zbus.transport.http.Http; 
 
//@Filter("logger")
public class InterfaceExampleImpl implements InterfaceExample{
	
	@Filter(exclude="logger")
	public String echo(String string) { 
		return string;
	}
	 
	public String getString(@Param("name") String name) { 
		if(name == null){
			System.out.println("got null: "+ name);
		}
		return "Hello World ZBUS " + name;
	} 
	
	public String testEncoding() { 
		return "中文";
	}
	
	public String[] stringArray() {
		return new String[]{"hong", "leiming"};
	} 
	
	public byte[] getBin() {
		return new byte[10];
	}
	
	public Object[] objectArray(String id){  
		return new Object[]{id, getUser("rushmore"), "hong", true, 1, String.class};
	}
	
	
	public int plus(int a, int b) { 
		return a+b;
	} 

	public MyEnum myEnum(MyEnum e) {  
		return MyEnum.Sunday;
	}
	
	
	public User getUser(String name) {
		User user = new User();
		user.setName(name);
		user.setPassword("password"+System.currentTimeMillis());
		user.setAge(new Random().nextInt(100));
		user.setItem("item_1");
		user.setRoles(Arrays.asList("admin", "common"));	
		user.getAttrs().put("extAttr1", "XAttr1");
		user.getAttrs().put("extAttr2", "XAttr2");
		return user;
	}
	
	
	public Order getOrder() {
		Order order = new Order();
		order.setItem(Arrays.asList("item1","item2","item3"));
		order.setData("bin".getBytes());
		return order;
	}
	
	
	public User[] getUsers() {
		return new User[]{getUser("hong"), getUser("leiming")};
	}
	 
	public List<User> listUsers() {
		return Arrays.asList(getUser("hong"), getUser("leiming"));
	} 
	 
	public List<Map<String, Object>> listMap() {
		List<Map<String, Object>> res = new ArrayList<Map<String,Object>>();
		res.add(map(1));
		res.add(map(2));
		res.add(map(3));
		return res;
	}
	
	
	public int saveObjectArray(Object[] array) {
		return 0;
	}


	public int saveUserArray(User[] array) { 
		return 0;
	}


	public int saveUserList(List<User> array) { 
		return 0;
	}
	
	public void throwException() {
		throw new RuntimeException("runtime exception from server");
	}
	
	public void throwUserException() throws UserException {
		throw new UserException("user defined exception");
	}
	
	public void throwNullPointerException(){
		throw new NullPointerException("null pointer");
	}
	
	public void throwUnkownException() {  
		throw new PrivateRuntimeException("private runtime exeption");
	}
	
	
	public void noReturn() {
		System.out.println("called noReturn");
	}
	
	
	public Class<?> classTest(Class<?> inClass) { 
		return Double.class;
	}

	public void testTimeout() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) { 
			e.printStackTrace();
		}
	}

	public int getUserScore() { 
		Random r = new Random(System.currentTimeMillis());
		int time = 10 + r.nextInt(100); 
		return time;
	}
	
	public String nullParam(String nullStr) { 
		return nullStr;
	}

	public Map<String, Object> map(int value1) { 
		Map<String, Object> value = new HashMap<>();
		value.put("key", value1);
		return value;
	} 
	
	public Message html() {
		Message res = new Message();
		res.setStatus(200);
		res.setHeader(Http.CONTENT_TYPE, "text/html; charset=utf8");
		res.setBody("<h1>html" + System.currentTimeMillis()+"</h1>");
		return res;
	} 
	
	@Route("/test")
	public Message req(Message req) {
		System.out.println(JSON.toJSONString(req, true));
		
		Message res = new Message();
		res.setStatus(200);
		res.setHeader(Http.CONTENT_TYPE, "text/html; charset=utf8");
		res.setBody("<h1>request injected</h1>");
		return res;
	}
	
	public Object testOverride(String data) {
		return "testOverride(String data)" + data;
	}
	
	public Object testOverride(Map<String, Object> data) {
		return data;
	}
	
	public Object testOverride(Object data) {
		return "Object:" + data;
	}
	
	public Object testOverride(int i, Map<String, Object> data) {
		return "testOverride(int i, Map<String, Object> data)" + data;
	}
	
	public Object testOverride() {
		return "testOverride()";
	}
	
	
	public Object getContext(@Param(ctx=true) Map<String, Object> ctx) {
		return ctx;
	}
	
	public Object threadContext() {
		return InvocationContext.getRequest().getContext();
	} 
	
	ScriptEngineManager factory = new ScriptEngineManager();
	ScriptEngine engine = factory.getEngineByName("javascript");
	@Override
	public Object callJs() { 
		try {  
			engine.eval(
					" function hi(){"
					+ "		var a = 'PROSPER'.toLowerCase(); "
					+ " 	middle(); "
					+ " 	//print('Live long and' + a)"
					+ "} "
					+ "function middle(){"
					+ "		var b = 1; "
					+ "		for(var i=0, max = 5; i<max;i++){"
					+ "			b++;"
					+ "		} "
					+ "		//print('b is '+b);"
					+ "}"
					+ "hi();"
				);
		} catch (ScriptException ex) {
			// ...
		}
		return null;
	}
	
	private void hi() {
		"PROSPER".toLowerCase();
		middle();
	}
	private int middle() {
		int b = 1;
		for(int i=0, max=5; i<max; i++) {
			b++;
		} 
		return b;
	}
	public Object callJava() { 
		hi();
		return null;
	}
	 
	public Message testCookie() { 
		Message response = new Message();
		List<String> cookies = new ArrayList<>(); 
		cookies.add("token1=abc; path=/;");
		cookies.add("token2=abc; path=/;");
		
		response.setHeader("Set-Cookie", cookies);
		
		response.setBody("check cookie mate!");
		
		return response;
	}
}



class PrivateRuntimeException extends RuntimeException{  
	private static final long serialVersionUID = 4587336984841564800L;

	public PrivateRuntimeException() {
		super(); 
	}

	public PrivateRuntimeException(String message, Throwable cause) {
		super(message, cause); 
	}

	public PrivateRuntimeException(String message) {
		super(message); 
	}

	public PrivateRuntimeException(Throwable cause) {
		super(cause); 
	}
	
}