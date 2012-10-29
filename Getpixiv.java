import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.io.File;

import javax.swing.*;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

//test
class Bass{
	public static int num = 400;
}




public class Getpixiv extends JFrame implements ActionListener {	
	JTextField name_text;
	JButton btn;
	String name;
	public Getpixiv(){
	      getContentPane().setLayout(new FlowLayout());
	      
	      name_text = new JTextField("NAME",30);
	      name_text.setFont(new Font("SansSerif", Font.ITALIC, 15));
	      getContentPane().add(name_text);
	      
	      btn = new JButton("取得");
	      btn.setFont(new Font("SansSerif", Font.ITALIC, 20));
	      btn.setBounds(200,100,300,50);
	      btn.addActionListener(this);
	      getContentPane().add(btn);
	      
	      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      setTitle("画像採るよ");
	      setSize(530, 150);
	      setVisible(true);

	   }
	   public void actionPerformed(ActionEvent e) {
		   name = name_text.getText();
		   new P_getter(name);
	   }
	   public static void main(String[] args) {
	      new Getpixiv();
	   }
}

class MakeDirectory{
	MakeDirectory(){
		File file = new File("C:\\GetPixiv");
		if ( !file.exists() ) {
				file.mkdir();
		}
	}
	MakeDirectory(String name){
		File file = new File("C:\\GetPixiv\\" +name);
		if ( !file.exists() ) {
				file.mkdir();
		}
	}
}




class P_getter{
    public P_getter(String name){
    	new MakeDirectory();
    	new MakeDirectory(name);
		Getter g = new Getter();
		String user_url = "null";
		String one_id = "null";
		String one_url = "null";
		String[] new_id = new String[Bass.num];
		
		try {
			 user_url = g.HTML_Getter(new URL("http://www.pixiv.net/member.php?id=" +name));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		Find_one f = new Find_one();
		one_id = f.one(user_url);
		
		try {
			 one_url = g.HTML_Getter(new URL("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" +one_id));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		Own_data o = new Own_data();
		Find_next n = new Find_next();
		new_id[0] = o.owndata(one_url);
		int index = 1;
		

		String users_data = o.owndata(one_url).substring(0,o.owndata(one_url).lastIndexOf("/"));
		
		String rear_url = one_url;
		String test;
		while(true){
			one_id = n.newer(one_url);
			try {
				 one_url = g.HTML_Getter(new URL("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" +one_id));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			test = o.owndata(one_url).substring(0,o.owndata(one_url).lastIndexOf("/"));
			if(!test.equals(users_data)) break;
			new_id[index++] = o.owndata(one_url);
		}
		
		while(true){
			one_id = n.older(rear_url);
			try {
				 rear_url = g.HTML_Getter(new URL("http://www.pixiv.net/member_illust.php?mode=medium&illust_id=" +one_id));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			test = o.owndata(rear_url).substring(0,o.owndata(rear_url).lastIndexOf("/"));
			if(!test.equals(users_data)) break;
			new_id[index++] = o.owndata(rear_url);	
		}
		

		//書き込み呼び出し
		for(int i=0;i<index;i++){
			try{
				new WImage(new_id[i], name);
			}catch(IOException e){
				int t=0;
				int y=0;
				System.out.println(e);
				while(y == 0){
					try {
						new WImage2(new_id[i], name, t);
					}catch(IOException e1){
						System.out.println(e1);
						y = 1;
					} catch (Exception e1) {
						e1.printStackTrace();
						t++;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
    }
}






class Find_one{
	public String one(String url){
		int index1 = 0, index2 = 0;
		String id = null;
		index1 = url.indexOf("member_illust.php?mode=medium&amp;illust_id=");
		index1 = index1 + 44;
		index2 = url.indexOf("\"", index1);
		id = url.substring(index1, index2);
		return id;
	}
}






class Find_next{
	public String newer(String url){
		int index1 = 0, index2 = 0;
		String id = null;
		index1 = url.indexOf("link-newer link-2ways");
		index1 = index1 + 76;
		index2 = url.indexOf("\"", index1);
		id = url.substring(index1, index2);
		return id;
	}
	
	public String older(String url){
		int index1 = 0, index2 = 0;
		String id = null;
		index1 = url.indexOf("link-older link-2ways");
		index1 = index1 + 76;
		index2 = url.indexOf("\"", index1);
		id = url.substring(index1, index2);
		return id;
	}
}


class Own_data{
	public String owndata(String url){
		int index1 = 0, index2 = 0;
		String id = null;
		index1 = url.indexOf("\"><img src=\"http://");
		index1 = url.indexOf("\"><img src=\"http://",index1+1000);
		index1 = index1 + 12;
		index2 = url.indexOf("alt=", index1)-3;
		id = url.substring(index1, index2+1);
		return id;
	}
}


//htmlを取得する
class Getter{
	public String HTML_Getter(URL url){
		String charset = "Shift_JIS";
		JTextArea htmlArea;
		String html_data;
		
		htmlArea = new JTextArea();
        // Webページを読み込む
        try {
            // 接続
            URLConnection uc = url.openConnection();
            uc.setRequestProperty("Accept-Language", "ja;q=0.7,en;q=0.3");
            uc.setRequestProperty("User-agent","Mozilla/5.0");
            
            // HTMLを読み込む
            BufferedInputStream bis = new BufferedInputStream(uc.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(bis, charset));
            htmlArea.setText("");//初期化
            String line;
            while ((line = br.readLine()) != null) {
                htmlArea.append(line + "\n");
            }
        } catch (MalformedURLException ex) {
            htmlArea.setText("URLが不正です。");
            ex.printStackTrace();
        } catch (UnknownHostException ex) {
            htmlArea.setText("サイトが見つかりません。");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        html_data = new String(htmlArea.getText());
        return html_data;
    }
}







class WImage{
	public WImage(String id, String name) throws Exception {
		id = id.replace("_m", "");
		System.out.println(id);
		URL url = new URL(id);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-agent","Mozilla/5.0");
		con.setRequestProperty("Referer","http://www.pixiv.net/member_illust.php?mode=big&illust_id=" +id);

		InputStream in = con.getInputStream();
		id = id.substring((id.lastIndexOf("/")+2)-1);
		OutputStream out = new FileOutputStream("C:\\GetPixiv\\" + name +"\\" +id);

		try{
			byte[] buf = new byte[1024];
			int len = 0;
			
			while ((len = in.read(buf))!=0){
				out.write(buf, 0, len);
			}
			out.flush();
		} finally {
			out.close();
			in.close();
		}
	}
}

class WImage2{
	public WImage2(String id, String name, int t) throws Exception {
		String tt = Integer.toString(t);
		id = id.replace("_m", "_p" +tt);
		System.out.println(id);
		URL url = new URL(id);
		URLConnection con = url.openConnection();
		con.setRequestProperty("User-agent","Mozilla/5.0");
		con.setRequestProperty("Referer","http://www.pixiv.net/member_illust.php?mode=big&illust_id=" +id);

		InputStream in = con.getInputStream();
		id = id.substring((id.lastIndexOf("/")+2)-1);
		OutputStream out = new FileOutputStream("C:\\GetPixiv\\" + name +"\\" +id);
		try{
			byte[] buf = new byte[1024];
			int len = 0;
			
			while ((len = in.read(buf))!=0){
				out.write(buf, 0, len);
			}
			out.flush();
		} finally {
			out.close();
			in.close();
		}
	}
}
