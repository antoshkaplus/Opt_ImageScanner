import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.security.*;

import javax.swing.*;

public class ImageScannerVis {
    int W,H,nb,nLetter;
    char[][] image;

    volatile boolean openRows[];
    volatile int nScans, maxScans;           //number of scans done and max number of scans
    boolean ok;                     //indicates that all scans were valid
    String errmes;                  //error message from scans
    // -----------------------------------------
    void generate(long seed) {
      try {
        //generate test case
        int i,j,k;
        SecureRandom r1 = SecureRandom.getInstance("SHA1PRNG"); 
        r1.setSeed(seed);
        H = r1.nextInt(251) + 50;
        W = r1.nextInt(251) + 50;
        if (seed==1)
            W = H = 30;
        System.out.println("H = "+H);
        System.out.println("W = "+W);

        int minLetter = W*H/400, maxLetter = W*H/200;
        nLetter = r1.nextInt(maxLetter-minLetter+1)+minLetter;
        System.out.println("Number of letters = "+nLetter);

        //generate the image itself
        image = new char[H][W];
        for (i=0; i<H; i++)
        for (j=0; j<W; j++)
            image[i][j]='0';
        //keep track of rectangles around already placed letters
        //each next rectangle shouldn't intersect any of previous ones too much
        boolean small;
        int placedSize[] = new int[nLetter], intSize, tries, h,w,x,y;
        Rectangle placed[] = new Rectangle[nLetter], interRect;
        String name,line;
        for (i=0; i<nLetter; i++) {
            //generate next filename
            name = (char)('A'+r1.nextInt(26))+""+(r1.nextInt(2)==0 ? 'P' : 'B')+(r1.nextInt(21)+8);
            //open corresponding file and read the letter dimensions from it
            BufferedReader br = new BufferedReader(new FileReader("letters/"+name+".txt"));
            h = Integer.parseInt(br.readLine());
            w = Integer.parseInt(br.readLine());
            placed[i] = new Rectangle(w,h);
            placedSize[i] = w+h;
            tries = 0;
            do {
                placed[i].setLocation(r1.nextInt(W-w+1), r1.nextInt(H-h+1));
                small = true;
                for (j=0; j<i && small; j++) {
                    interRect = placed[j].intersection(placed[i]);
                    intSize = (int)(interRect.getWidth()+interRect.getHeight());
                    if (!interRect.isEmpty() && (2*intSize > placedSize[i] || 2*intSize > placedSize[j]))
                        small = false;
                }
                tries ++;
            }
            while (!small && tries < 100);
            //read the actual image and write it to the overall picture immediately
            y = (int)placed[i].getY();
            x = (int)placed[i].getX();
            for (k=y; k<y+h; k++) {
                line = br.readLine();
                for (j=x; j<x+w; j++)
                    if (line.charAt(j-x)=='1')
                        image[k][j] = '1';
            }
        }

        //calculate scalar parameters
        nb = 0;
        for (i=0; i<H; i++)
        for (j=0; j<W; j++)
            if (image[i][j] == '1')
                nb ++;
        System.out.println("Number of black pixels = "+nb);
        
        openRows = new boolean[H];
        maxScans = H;
        nScans = 0;
        ok = true;
      }
      catch (Exception e) {
        System.err.println("An exception occurred while generating test case.");
        e.printStackTrace(); 
      }
    }
    // -----------------------------------------
    public String scan(int row) {
        if (row < 0 || row >= H) {
            errmes = "Invalid row specified for scanning.";
            ok = false;
            return "";
        }
        if (nScans == maxScans) {
            errmes = "Number of scans exceeded the maximal number of scans avaliable.";
            ok = false;
            return "";
        }
        if (openRows[row]) {
            errmes = "Second scan of row "+row+" attempted.";
            ok = false;
            return "";
        }
        nScans ++;
        openRows[row] = true;
        for (int i=0; i<W; i++)
        	openImage[row][i] = image[row][i];
        draw();
        return new String(image[row]);
    }
    // -----------------------------------------
    public double runTest(String seed) {
      try {
        generate(Long.parseLong(seed));
        int i,j;

		openImage = new char[H][W];
        if (vis)
        {   jf.setSize((W+2)*SZ+50,H*SZ+40);
            jf.setVisible(true);
            for (i=0; i<H; i++)
            for (j=0; j<W; j++)
                openImage[i][j] = '0';
            highlightRow = -1;
            ready = false;
            draw();
        }

        //call the solution
        String[] imageRet = restore(H,W,nb,nLetter);

        if (!ok) {               //something went wrong during scans
            addFatalError(errmes);
            return 0.0;
        }

        //check the return and score it
        if (imageRet == null || imageRet.length != H) {
            addFatalError("Your return contained invalid number of elements.");
            return 0.0;
        }
        for (i=0; i<H; i++)
            if (imageRet[i] == null || imageRet[i].length() != W) {
                addFatalError("Element "+i+" of your return contained invalid number of characters."+imageRet[i]);
                return 0.0;
            }

        for (i=0; i<H; i++)
        for (j=0; j<W; j++)
            if (imageRet[i].charAt(j) != '0' && imageRet[i].charAt(j) != '1') {
                addFatalError("Character ["+i+"]["+j+"] of your return was invalid.");
                return 0.0;
            }

        int score = 0, allWhiteScore = 0;
        for (i=0; i<H; i++)
            if (!openRows[i])
                for (j=0; j<W; j++) {
                    if (imageRet[i].charAt(j) == image[i][j])
                        score ++;
                    if (image[i][j] == '0')
                        allWhiteScore ++;
                }

        //only guesses in rows which weren't open count
        //and the score is given as improvement over allWhiteScore
        return Math.max(0,(score-allWhiteScore)*1.0/nb);
      }
      catch (Exception e) { 
        System.err.println("An exception occurred while trying to get your program's results.");
        e.printStackTrace(); 
        return 0;
      }
    }
// ------------- visualization part ------------
    JFrame jf;
    Vis v;
    static String exec;
    static boolean vis, manual, hint;
    static Process proc;
    static int del;                                                //delay
    InputStream is;
    OutputStream os;
    BufferedReader br;
    static int SZ;
    volatile boolean ready;
    volatile int highlightRow;
    volatile char openImage[][];
    // -----------------------------------------
    String[] restore(int H, int W, int nb, int nLetter) throws IOException {
        if (!manual && proc != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(H+"\n").append(W+"\n").append(nb+"\n").append(nLetter+"\n");
            os.write(sb.toString().getBytes());
            os.flush();

            //simulate scan requests for the solution
            String s;
            int row;
            while ((s = br.readLine()).equals("?")) {
                //get param of next call
                row = Integer.parseInt(br.readLine());
                s = scan(row);
                if (!ok)
                    return new String[0];
                //visualize
                draw();
                //and return the result to the solution
                os.write((s+"\n").getBytes());
                os.flush();
            }
        }

        //and get the return value
        String[] ret = new String[H];
        int i;
        if (manual) {
            while (!ready)
            {   try { Thread.sleep(50);}
                catch (Exception e) { e.printStackTrace(); } 
            }
            //convert openImage to ret
            for (i=0; i<H; i++)
                ret[i] = new String(openImage[i]);
        }
        else if (proc != null) {
            for (i=0; i<H; i++) {
                ret[i] = br.readLine();
                if (!openRows[i]) {
                	for (int j=0; j<W; j++)
                		openImage[i][j] = ret[i].charAt(j);
                }
            }
            draw();
            System.out.println("done");
        }
        return ret;
    }
    // -----------------------------------------
    void draw() {
        if (!vis) return;
        v.repaint();
        try { Thread.sleep(del); }
        catch (Exception e) { };
    }
    // -----------------------------------------
    public class Vis extends JPanel implements MouseListener, MouseMotionListener, WindowListener {
        public void paint(Graphics g) {
            int i,j;
            //do painting here
            BufferedImage bi = new BufferedImage((W+2)*SZ+50,H*SZ+40,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = (Graphics2D)bi.getGraphics();
            //background
            g2.setColor(new Color(0xDDDDDD));
            g2.fillRect(0,0,(W+2)*SZ+50,H*SZ+40);
            g2.setColor(Color.WHITE);
            g2.fillRect(0,0,W*SZ,H*SZ);

            //highlight row with current mouse position
            if (highlightRow > -1) {
                g2.setColor(new Color(0xDDDDDD));
                g2.fillRect(0,highlightRow*SZ,W*SZ,SZ);
            }

            //if the player requested a hint, draw the real board
            if (hint) {
                g2.setColor(new Color(0xCCCCFF));
                for (i=0; i<H; i++)
                for (j=0; j<W; j++)
                    if (image[i][j]=='1')
                        g2.fillRect(j*SZ,i*SZ,SZ,SZ);
            }

            //draw rows which are already open
            //and cells which are marked as filled by the player
            for (i=0; i<H; i++) {
                if (openRows[i]) 
                    g2.setColor(Color.BLACK);
                else
                    g2.setColor(new Color(0x888888));
                for (j=0; j<W; j++)
                    if (openImage[i][j]=='1')
                        g2.fillRect(j*SZ,i*SZ,SZ,SZ);
            }

            //lines between pixels
            g2.setColor(new Color(0xDDDDDD));
            for (i=0; i<=H; i++)
                g2.drawLine(0,i*SZ,W*SZ,i*SZ);
            for (i=0; i<=W; i++)
                g2.drawLine(i*SZ,0,i*SZ,H*SZ);

            //number of rows left
            g2.setColor(Color.BLACK);
            char ch[] = (""+(maxScans-nScans)).toCharArray();
            g2.setFont(new Font("Arial",Font.BOLD,14));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawChars(ch,0,ch.length,(W+2)*SZ,SZ+fm.getHeight());

            g.drawImage(bi,0,0,(W+2)*SZ+50,H*SZ+40,null);
        }
        // -------------------------------------
        public Vis() {
            addMouseListener(this);
            addMouseMotionListener(this);
            jf.addWindowListener(this);
        }
        // -------------------------------------
        //WindowListener
        public void windowClosing(WindowEvent e){ 
            if(proc != null)
                try { proc.destroy(); } 
                catch (Exception ex) { ex.printStackTrace(); }
            System.exit(0); 
        }
        public void windowActivated(WindowEvent e) { }
        public void windowDeactivated(WindowEvent e) { }
        public void windowOpened(WindowEvent e) { }
        public void windowClosed(WindowEvent e) { }
        public void windowIconified(WindowEvent e) { }
        public void windowDeiconified(WindowEvent e) { }
        // -------------------------------------
        //MouseListener
        public void mouseClicked(MouseEvent e) {
            //for manual play
            if (!manual || ready) return;

            //right-click submits the current state of the board
            if (e.getButton() != MouseEvent.BUTTON1) {
                manual = false;
                ready = true;
                return;
            }

            //convert to args only clicks with valid coordinates
            highlightRow = -1;
            int row = e.getY()/SZ, col = e.getX()/SZ;
            if (row>=0 && row<H && col>=0 && col<W && !openRows[row]) {
                //toggle the state of the currently open pixel
                openImage[row][col] = (char)('0'+'1'-openImage[row][col]);
                repaint();
            }
            if (row>=0 && row<H && col>=W && !openRows[row]) {
                //scan the current row
                scan(row);
                if (!ok)
                    ready = true;
            }
        }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        // -------------------------------------
        //MouseMotionListener
        public void mouseMoved(MouseEvent e) { 
            if (!manual || ready) return;
            int row = e.getY()/SZ, col = e.getX()/SZ;
            if (row>=0 && row<H && col>=W && !openRows[row]) {
                if (row!=highlightRow) {
                    highlightRow = row;
                    v.repaint();
                }
            }
            else {
                highlightRow = -1;
                v.repaint();
            }
        }
        public void mouseDragged(MouseEvent e) {}
    }
    // -----------------------------------------
    public ImageScannerVis(String seed) {
      try {
        //interface for runTest
        if (vis)
        {   jf = new JFrame();
            v = new Vis();
            jf.getContentPane().add(v);
        }
        if (exec != null) {
            try {
                Runtime rt = Runtime.getRuntime();
                proc = rt.exec(exec);
                os = proc.getOutputStream();
                is = proc.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                new ErrorReader(proc.getErrorStream()).start();
            } catch (Exception e) { e.printStackTrace(); }
        }
        System.out.println("Score = "+runTest(seed));
        if (proc != null)
            try { proc.destroy(); } 
            catch (Exception e) { e.printStackTrace(); }
      }
      catch (Exception e) { e.printStackTrace(); }
    }
    // -----------------------------------------
    public static void main(String[] args) {
        String seed = "1";
        vis = true;
        manual = hint = false;
        del = 100;
        SZ = 3;
        for (int i = 0; i<args.length; i++)
        {   if (args[i].equals("-seed"))
                seed = args[++i];
            if (args[i].equals("-exec"))
                exec = args[++i];
            if (args[i].equals("-delay"))
                del = Integer.parseInt(args[++i]);
            if (args[i].equals("-novis"))
                vis = false;
            if (args[i].equals("-manual"))
                manual = true;
            if (args[i].equals("-size"))
                SZ = Integer.parseInt(args[++i]);
            if (args[i].equals("-hint"))
                hint = true;
        }
        if (seed.equals("1")) SZ=9;
        if (SZ>9) SZ=9;
        if (exec == null)
            manual = true;
        if (manual)
            vis = true;
        ImageScannerVis f = new ImageScannerVis(seed);
    }
    // -----------------------------------------
    void addFatalError(String message) {
        System.out.println(message);
    }
}

class ErrorReader extends Thread{
    InputStream error;
    public ErrorReader(InputStream is) {
        error = is;
    }
    public void run() {
        try {
            byte[] ch = new byte[50000];
            int read;
            while ((read = error.read(ch)) > 0)
            {   String s = new String(ch,0,read);
                System.out.print(s);
                System.out.flush();
            }
        } catch(Exception e) { }
    }
}
