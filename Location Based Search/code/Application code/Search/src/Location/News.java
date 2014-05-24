package Location;


/**
 *  
 * @author pratik
 */

public class News {
protected String title;
protected String summary;
protected String time;
protected String type;
protected String latlng; 
protected int rank;
public News()
{
 this.time=new String();
 this.title=new String();
 this.summary=new String();
 this.type=new String();
 this.latlng=new String();
 this.rank=0;
	
}
public News(String title,String time,String type,String latlng,String summary,int rank)
 {
	this.title=title;
	this.summary=summary;
	this.time=time;
	this.type=type;
	this.latlng=latlng;
	this.rank=rank;
 }

}
