package org.dykman.example.springboot;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.OPTIONS;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.sql.DataSource;
import javax.websocket.server.PathParam;

import org.dykman.example.DbUtil;
import org.dykman.example.ExampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@RestController

// the path here is the base path for the endpoints which are methods in this class
// the endpoint methods themselves typically extend that path 
@RequestMapping(name = "survey", path = "/")
@ControllerAdvice()
public class SBStubController {
	public static final String TYPE_JSON = "application/json;charset=utf8";
//	public static final String JWT_TOKEN = JwtManager.HTTP_HEADER;
//	public static final String ADMIN_JWT_TOKEN = JwtManager.HTTP_HEADER_ADMIN;
	private final static String ADMIN_USER_ID = "EDIARYADM";
	public static final String ACCESS_CONTROL_MAX_AGE = "86400";
	public static final String REPORTTABLE = "report120";
	public static final int STUDY_ID = 120;
	
	static Logger LOG = LoggerFactory.getLogger(SBStubController.class);

	
	@Autowired
	private ApplicationContext context;

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	Environment environment;

	ParameterNameDiscoverer pmd = new DefaultParameterNameDiscoverer();
	protected String klassName = getClass().getName();

	public SBStubController() {
	}

	// cheap and unsafe allow-all for CORS pre-flight
	@RequestMapping(name = "options", path = "/**", method = OPTIONS)
	public ResponseEntity<Object> options() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "POST,GET,PUT,PATCH,DELETE,OPTIONS");
		headers.add("Access-Control-Max-Age", ACCESS_CONTROL_MAX_AGE);
//		headers.add("Access-Control-Allow-Headers", JwtManager.HTTP_HEADER + ", " + JwtManager.HTTP_HEADER_ADMIN);

		return new ResponseEntity<Object>("",headers, HttpStatus.OK);
	}
	

	@GetMapping(name = "testClaims", path = "/claims", produces = TYPE_JSON)
	// because there is only 1 object of type Claims declared in the applicationContext (in BeanDefinitions)
	// this object is provided by magic through BeanDefinitions method.
	public ResponseEntity<Object> testClaims(Claims claims) throws Exception {
		return instrument("testClaims",() ->{
				return new LinkedHashMap<>(claims);
			});
	}
	
	
	@GetMapping(name = "answers", path = "/example/{id}", produces = TYPE_JSON)
	public ResponseEntity<Object> simpleGetEndpoint(@PathVariable("id") String userId) throws Exception {
		return instrument("survey", () -> {

			DataSource ds = context.getBean("datasource", DataSource.class);


			try (Connection c = ds.getConnection()) {
				int i = Integer.parseInt(userId);

				ExampleService exampleService = context.getBean(ExampleService.class);
				
				Object answers = exampleService.doSomethingToTheUser(c, i);
				if (answers != null) {
					return packageOutput("answers", answers);
				} else {
					return new ResponseEntity<>(packageOutput("not-found",
							quickStatus("no answers found for that item")),HttpStatus.NOT_FOUND);
				}
			}
		});
	}
	

	// add new answers

	/*****************
	 * 
	 * Events
	 */
	@GetMapping(name = "events", path = "/events", produces = TYPE_JSON)
	
	/******** boilerplate below ***********/
	private List<String> checkConfiguration() {
		List<String> errs = new ArrayList<>();

		String user = System.getenv("DB_USER");
		if (user == null)
			errs.add("missing environment variable: DB_USER");

		String password = System.getenv("DB_PASSWORD");
		if (password == null)
			errs.add("missing environment variable: DB_PASSWORD");

		String host = System.getenv("DB_HOST");
		if (host == null)
			errs.add("missing environment variable: DB_HOST");

		String port = System.getenv("DB_PORT");
		if (port == null)
			port = "1521";

		String schema = System.getenv("DB_SCHEMA");
		if (schema == null)
			errs.add("missing environment variable: DB_SCHEMA");

		// create a listand add messages to it only in the event of configuration errors
		return errs.size() > 0 ? errs : null;
	}

	@ExceptionHandler({ NotFoundException.class })
	public ResponseEntity<Object> notfound(NotFoundException e) {
		Map<String, Object> m = new HashMap<>();
		m.put("message", e.getMessage());
		return new ResponseEntity(m, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler({ Exception.class })
	public ResponseEntity<Object> error(Exception e) {
		e.printStackTrace();
		Map<String, Object> m = new HashMap<>();
		m.put("message", "general: " + e.getClass().getName() + ": " + e.getLocalizedMessage());
		return new ResponseEntity(m, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	
	// this is a magic endpoint that generates a list of all of the endpoints included in this servicde
	@GetMapping(name = "index", path = "/", produces = TYPE_JSON)
	public ResponseEntity<Object> index() throws Exception {
		System.out.println("SurveyController - index");
		return instrument("index", () -> {

			List<String> err = checkConfiguration();
			if (err != null) {
				return err;
			}
			Map<String, Object> r = new LinkedHashMap<>();
			Map<RequestMappingInfo, HandlerMethod> mm = requestMappingHandlerMapping.getHandlerMethods();
			for (Map.Entry<RequestMappingInfo, HandlerMethod> rinfo : mm.entrySet()) {
				RequestMappingInfo rmi = rinfo.getKey();
				HandlerMethod v = rinfo.getValue();
				String name = rmi.getName();
				if (name == null)
					name = Integer.toString(rmi.hashCode());
				if (name.indexOf('#') > 0) {
					String[] sp = name.split("#");
					if (sp.length > 1)
						name = sp[1];
				}
				Map<String, Object> data = new LinkedHashMap<>();
				data.put("path-params", describeClass(v, PathParam.class));
				Map<String, Object> dd = describeClass(v, RequestParam.class);
				dd.putAll(describeClass(v, RequestPart.class));
				data.put("params", dd);

				data.put("path", String.join(",", rmi.getPatternsCondition().getPatterns()));
				Set<RequestMethod> rmms = rmi.getMethodsCondition().getMethods();
				Set<String> rn = new LinkedHashSet<>();
				if (rmms == null || rmms.size() == 0) {
					for (HttpMethod rm : HttpMethod.values()) {
						rn.add(rm.name());
					}
				} else {
					for (RequestMethod rm : rmms) {
						rn.add(rm.name());
					}
				}
				data.put("method", String.join(",", rn));

				StringBuilder sb;
				List<String> ll = new ArrayList<>();
				for (MediaType mt : rmi.getProducesCondition().getProducibleMediaTypes()) {
					sb = new StringBuilder();
					sb.append(mt.getType()).append('/').append(mt.getSubtype());
					Charset cs = mt.getCharset();
					if (cs != null) {
						sb.append(' ').append(cs.displayName());
					}
					ll.add(sb.toString());
				}
				data.put("produces", ll);
				ll = new ArrayList<>();
				for (MediaType mt : rmi.getConsumesCondition().getConsumableMediaTypes()) {
					sb = new StringBuilder();
					Charset charset = mt.getCharset();
					sb.append(mt.getType()).append('/').append(mt.getSubtype());
					if (charset != null)
						sb.append(' ').append(charset.displayName());
					ll.add(sb.toString());
				}
				if (ll.size() > 0)
					data.put("consumes", ll);
				r.put(name, data);
			}
			return r;
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ResponseEntity<Object> instrument(String name, ModelRequest func) throws Exception {
		MetricRegistry metricRegistry = context.getBean("metricRegistry", MetricRegistry.class);

		Counter acounter = metricRegistry.counter(MetricRegistry.name(klassName, name, "requests"));
		acounter.inc();
		Timer.Context tc = metricRegistry.timer(MetricRegistry.name(klassName, name, "execution")).time();
		try {
			Object o = func.apply();
			if (o == null)
				return null;
			if (o instanceof ResponseEntity)
				return (ResponseEntity) o;
			
			// CORS header
			HttpHeaders hp = new HttpHeaders();
			
			return new ResponseEntity<>(o, hp, HttpStatus.OK);
			
		} catch (JwtException e) {
			
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			
		} catch (Exception e) {
			metricRegistry.counter(MetricRegistry.name(klassName, name, "exceptions")).inc();
			throw e;
		} finally {
			acounter.dec();
			tc.stop();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map<String, Object> describeClass(HandlerMethod v, Class klass) {
		MethodParameter[] mps = v.getMethodParameters();

		Map<String, Object> m = new LinkedHashMap<>();
		for (MethodParameter mp : mps) {
			boolean annotationed = mp.hasParameterAnnotation(klass);
			if (annotationed) {
				mp.initParameterNameDiscovery(pmd);
				Class k = mp.getParameterType();
				String kname = (k.isAssignableFrom(MultipartFile.class)) ? "File" : k.getName();
				m.put(mp.getParameterName(), kname);
			}
		}
		return m;
	}

	protected interface ModelRequest {
		public Object apply() throws Exception;
	}

	private Object packageOutput(String objType, Object payload) {
		return packageOutput(objType, payload, System.nanoTime()); // backwards compatible
	}

	private Object packageOutput(String objType, Object payload, Long startTime) {
		Map<String, Object> data = new LinkedHashMap<>();
		Map<String, Object> meta = new LinkedHashMap<>();
		Map<String, Object> pl = new LinkedHashMap<>();

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();

		meta.put("currentTime", dtf.format(now));

		meta.put("executionTime", (System.nanoTime() - startTime) / (int) 1e+9 + " seconds");

		if (payload instanceof List) { // raw type
			List p = (List) payload;
			pl.put("count", p.size());
		}

		pl.put(objType, payload);

		data.put("meta", meta);
		data.put("payload", pl);

		return data;

	}


	Map<String,String> quickStatus(String msg) {
		Map<String,String> m = new LinkedHashMap<>();
		m.put("status", msg);
		return m;
	}
}
