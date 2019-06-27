package com.google.ext.springmvc.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.google.extspringmvc.extannotation.ExtController;
import com.google.extspringmvc.extannotation.ExtRequestMapping;
import com.google.utils.ClassUtil;

/**
 * 自定义前端控制器
 * 
 * @author wk
 *
 */
@SuppressWarnings("serial")
public class ExtDispatcherServlet extends HttpServlet {

	// mvc bean key=beanid ,value=对象
	private ConcurrentHashMap<String, Object> springMvcBeans = new ConcurrentHashMap<String, Object>();
	// mvc 请求方法 key=requestUrl,value=对象
	private ConcurrentHashMap<String, Object> mvcBeanUrl = new ConcurrentHashMap<String, Object>();
	// mvc 请求方法 key=requestUrl,value=方法
	private ConcurrentHashMap<String, String> mvcMethodUrl = new ConcurrentHashMap<String, String>();

	@Override
	public void init() throws ServletException {
		// 1.获取当前包下的所有的类
		List<Class<?>> classes = ClassUtil.getClasses("com.google.controller");
		try {
			// 2.将扫包范围所有的类，注入到springmvc容器里面，存放在Map集合中 key默认类名小写，value对象
			findClassMVCBeans(classes);
			// 3.将url映射与方法进行关联
			handlerMapping();
			System.out.println();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		//1.获取请求地址
		String url = req.getRequestURI();
		if(StringUtils.isEmpty(url)) {
			return;
		}
		//2.从map中获取
		Object object = mvcBeanUrl.get(url);
		if(object==null) {
			System.out.println("404 url");
			resp.getWriter().println("404 url");
			return;
		}
		String methodName = mvcMethodUrl.get(url);
		if(StringUtils.isEmpty(methodName)) {
			System.out.println("404 method");
			resp.getWriter().println("404 method");
			return;
		}
		//使用java反射机制调用方法
		String resultPage = (String)methonInvoke(object,methodName,req);
		//输出返回的信息，不返回界面
		//resp.getWriter().println(resultPage);
		extResourceViewResolver(resultPage,req,resp);
	}
	
	private void extResourceViewResolver(String pageName, HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		// 根路径
		String prefix = "/";
		String suffix = ".jsp";
		req.getRequestDispatcher(prefix + pageName + suffix).forward(req, res);
	}
	
	private Object methonInvoke(Object object,String methodName,HttpServletRequest req) {
	//3.通过url获取方法
		try {
			Class<? extends Object> classInfo = object.getClass();
			Method method = classInfo.getMethod(methodName,HttpServletRequest.class);
			Object result = method.invoke(object,req);
			return result;
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void findClassMVCBeans(List<Class<?>> classes)
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {

		for (Class<?> classInfo : classes) {
			// 判断类上是否有注解
			ExtController extController = classInfo.getAnnotation(ExtController.class);
			if (extController != null) {
				// 默认类名是小写
				String beanId = ClassUtil.toLowerCaseFirstOne(classInfo.getSimpleName());
				// 实例化对象
				Object object = ClassUtil.newInstance(classInfo);
				springMvcBeans.put(beanId, object);
			}
		}

	}

	public void handlerMapping() {
		// 1.获取springmvc bean容器对象
		// 2.遍历springmvc bean容器
		for (Map.Entry<String, Object> mvcBean : springMvcBeans.entrySet()) {
			Object object = mvcBean.getValue();
			// 判断类上是否有url映射注解
			Class<? extends Object> classInfo = object.getClass();
			ExtRequestMapping extRequestMapping = classInfo.getAnnotation(ExtRequestMapping.class);
			String baseUrl = "";
			if (extRequestMapping != null) {
				// 获取类上的url映射地址
				baseUrl = extRequestMapping.value();
			}
			// 3.遍历所有的方法上是否url映射注解,判断方法上是否有加url映射地址
			Method[] declaredMethods = classInfo.getDeclaredMethods();
			for (Method method : declaredMethods) {
				ExtRequestMapping annotation = method.getAnnotation(ExtRequestMapping.class);
				if(annotation!=null) {
					String methodUrl = baseUrl+annotation.value();
					mvcBeanUrl.put(methodUrl, object);
					mvcMethodUrl.put(methodUrl,method.getName());
				}
			}
		}

	}
}
