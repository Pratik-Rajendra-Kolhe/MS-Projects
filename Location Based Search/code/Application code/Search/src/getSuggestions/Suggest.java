package getSuggestions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Suggest
 */
@WebServlet("/suggest")
public class Suggest extends HttpServlet {
	private static final long serialVersionUID = 1L;    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Suggest() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub		
		System.out.println(new Date()+" "+request.getLocalName()+" "+request.getLocalAddr()+" "+request.getParameter("q"));
		String req = request.getParameter("q");
		String url = null;
		if(req.charAt(0) >= 'A' && req.charAt(0) <= 'Z')
			url = "http://localhost:8080/solr/locations/suggest?q="+req;
		else
			url = "http://localhost:8080/solr/wikinews/suggest?q="+req;
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 		con.setRequestMethod("GET");
 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
 		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
 		String str,xml="";
 		while((str=in.readLine()) != null)
 		{
 			xml += str;
 		}
 		Pattern p = Pattern.compile(".*?<arr(.*?)</arr>.*");
 		Matcher m=null;
 		if((m=p.matcher(xml)).find())
 		{
 			xml = m.group(1).replaceAll("</str>.*?<str>", ",").replaceFirst(".*?str>", "").replaceFirst("</str>.*?", ""); 			
 		}
 		else
 			xml="";
 		PrintWriter pw = response.getWriter();
		pw.print(xml);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
