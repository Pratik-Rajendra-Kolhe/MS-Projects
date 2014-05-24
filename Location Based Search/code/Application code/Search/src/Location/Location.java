package Location;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

/**
 * Servlet implementation class Location
 * @author pratik
 */
@WebServlet("/Location")
public class Location extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    public Location() {
        super();
        
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String def = "http://en.wikinews.org/wiki/";
		String query = request.getParameter("query");
		System.out.println(query);
		if(query!="" && query!=" ")
		{
		response.setContentType("text/html");  
	    PrintWriter pw = response.getWriter();
	    
	    pw.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
	    		+ "<html>"
	    		+ "<head>"
	    		+ "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">"
	    				+ "<title>False Positive Results</title>"
	    				+ "<link rel=\"stylesheet\""
	    				+ "href=\"./css/styles_results.css\""
	    						+ "type=\"text/css\"/>"
	    						+ "<script src=\"jquery-1.10.2.min.js\"></script>"
	    						+ "<script src=\"irproject3_search_ui_results.js\"></script>"
	    								+ "<script src=\"http://maps.googleapis.com/maps/api/js?key=AIzaSyC4NBdcabmtXIUa0Zy2jVJOFYDJKNty9Tk&sensor=false\"></script>"
	    								+ "</head>"
	    								+ "<body onload='initialize()'>"
	    								+ "<form name=\"Form1\" action=\"http://localhost:8888/Search/Location\">"
	    								+ "<!-- <div class=\"bgimg\"></div> -->"
	    								+ "<a class='goHome' href='http://localhost:8888/Search'>Home</a>"
	    								+ "<input type=\"text\" name=\"query\" size=\"70\" value=\"\" autocomplete=\"off\" \\>"
	    										+ "<input type=\"submit\" value=\"Search\" \\>"
	    										+ "<!--<div class=\"arrow-left1\"></div>"
	    										+ "<div class=\"arrow-left2\"></div>"
	    										+ "<div class=\"map-box\">Hello World Hello World</div>-->"
	    										+ "<div class=\"dropDown\" style=\"display:none;\">"
	    										+ "<div><input type=\"text\" readonly=\"readonly\" size=\"70\" value=\"\" autocomplete=\"off\" style=\"display:none\"\\></div>"
	    										+ "<div><input type=\"text\" readonly=\"readonly\" size=\"70\" value=\"\" autocomplete=\"off\" style=\"display:none\"\\></div>"
	    										+ "<div><input type=\"text\" readonly=\"readonly\" size=\"70\" value=\"\" autocomplete=\"off\" style=\"display:none\"\\></div>"
	    										+ "<div><input type=\"text\" readonly=\"readonly\" size=\"70\" value=\"\" autocomplete=\"off\" style=\"display:none\"\\></div>"
	    										+ "<div><input type=\"text\" readonly=\"readonly\" size=\"70\" value=\"\" autocomplete=\"off\" style=\"display:none\"\\></div>"
	    										+ "</div>"
	    										+ "</form>"
	    										+ "<br>"
	    										+ "<div class=\"output\">"
	    										+ "<div class=\"results\">");
	    
	    pw.println("<h3>Related Search Results for <i><small>" + query + "</small></i></h3>\n");
		String gmap = "block";
	    Map<Integer,News> results=QueryProcessor.getResults(query);
		if(results.size()==0)
		{
			gmap = "none";
			pw.println("<big>Sorry, No Results Found :(</big>");
		}
		Set<Integer> r= results.keySet();
		Iterator ri = r.iterator();
		String temp = "";
		boolean isFirst = true;
	    while(ri.hasNext())
	    {
	    	News t= results.get(ri.next());
	    	if(t.type=="R")
	    	{
	    		if(isFirst)
	    		{
	    			temp = t.latlng;
	    			isFirst = false;
	    		}
		    	String title1 = t.title.replaceAll(" ", "_");
		    	pw.println("<div class=\"result\">\n<a href=\"" + def+title1 + "\" class=\"title\">" + t.title + "</a><small style='color:#009900;font-size:0.95em'>"+t.time+"</small>\n<div class=\"summary\">" + t.summary + "...</div><br>");
	    	}
	    }
	    String qloc = temp;
	    System.out.println("qloc "+qloc);
	    pw.println("<h3>Other results from near by Locations</h3>");
	    if(results.size()==0)
		{
			pw.println("<big>Sorry, No Results Found :(</big>");
			
		}
	    Iterator ri1 = r.iterator();
	    temp += qloc!=""?";":"";
	    isFirst = true;
	    while(ri1.hasNext())
	    {
	    	News t= results.get(ri1.next());
	    	if(t.type=="L")
	    	{	    		
	    		if(!t.latlng.equals("") && t.latlng != null && !t.latlng.equals(qloc))
	    		{
	    			if(isFirst)
	    			{
	    				temp += "" + t.latlng;
	    				isFirst = false;
	    			}
	    			else
	    			{
	    				temp += ";" + t.latlng;
	    			}
	    			System.out.println(t.latlng);
	    		}
		    	String title1 = t.title.replaceAll(" ", "_"); 
		    	pw.println("<div class=\"result\">\n<a href=\"" + def+title1 + "\" class=\"title\">" + t.title + "</a><small style='font-size:0.95em'>"+t.time+"</small>\n<div class=\"summary\">" + t.summary + "...</div><br>");
	    	}
	    }
	    pw.println("<div id='dist' style='display:none'>"+QueryProcessor.d+"</div>");
	    pw.println("<div id='latlng' style='display:none'>"+temp+"</div>");
	    pw.println("</div><div id=\"googleMap\" style='display:"+gmap+"'>\n</div>\n"
	    		+ "</div>\n"
	    		+ "</body>\n"
	    		+ "</html>");
	}
		else
		{
			response.sendRedirect("");
		}
	}
	    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

}
