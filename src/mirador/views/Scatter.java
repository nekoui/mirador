/* COPYRIGHT (C) 2014 Fathom Information Design. All Rights Reserved. */

package mirador.views;

import java.util.ArrayList;

import miralib.data.DataSlice2D;
import miralib.data.Value2D;
import miralib.math.Numbers;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Scatter plot.
 *
 */

public class Scatter extends View {
  final static public boolean USE_ELLIPSES = true; 
  
  protected ArrayList<Value2D> points; 
  protected double weightSum;
  
  public Scatter(DataSlice2D slice) {
    super(slice.varx, slice.vary, slice.ranges);
    initPoints(slice);
  }

  public void draw(PGraphics pg) {
    pg.beginDraw();
    pg.background(WHITE);
    pg.noStroke();
    long nx, ny;
    float rad = 0;      
    int a;
    if (varx.categorical() && vary.categorical()) {
      a = 200;
    } else {
      nx = 0;
      ny = 0;
      int count = PApplet.min(500, points.size());
      rad = PApplet.map(count, 0, 500, 0.05f, 0.01f);
      a = (int)PApplet.map(count, 0, 500, 70, 10);
    }
    
    nx = varx.getCount(ranges);
    ny = vary.getCount(ranges);
    pg.fill(pg.red(BLUE), pg.green(BLUE), pg.blue(BLUE), a);
    for (Value2D pt: points) {
      float px, py;
      float pw, ph;            
      if (varx.categorical() && vary.categorical()) {
        if (Numbers.equal(pt.w, 0)) continue;
        //x = (float)(pg.width * pt.x);
        //y = pg.height - (float)(pg.height * pt.y);
        
        long i = (long)(pt.x * (nx-1));
        float dx = (float)pg.width / nx;
        px = dx/2 + dx * i;
        
        long j = (long)(pt.y * (ny-1));
        float dy = (float)pg.height / ny;
        py = pg.height - (dy/2 + dy * j);
        
        rad = PApplet.map(PApplet.sqrt((float)(pt.w / weightSum)), 0, 1, 0.05f, 0.5f); 
                       // PApplet.min((float)pg.width / (float)nx, 
                       //             (float)pg.height / (float)ny));
        pw = pg.width * rad;
        ph = pg.height * rad; 
      } else {
        if (varx.categorical()) {
          long i = (long)(pt.x * (nx-1));
          float dx = (float)pg.width / nx;
          px = dx/2 + dx * i;
        } else {
          px = (float)(pg.width * pt.x);  
        }
        
        if (vary.categorical()) {
          long j = (long)(pt.y * (ny-1));
          float dy = (float)pg.height / ny;
          py = pg.height - (dy/2 + dy * j);          
        } else {
          py = pg.height - (float)(pg.height * pt.y);  
        }      
        pw = pg.width * rad;
        ph = pg.height * rad;        
      }        
      if (USE_ELLIPSES) pg.ellipse(px, py, pw, ph);
      else pg.rect(px - pw/2, py - ph/2, pw, ph);
      
      // Only when hovering...
//      if (pt.label != null) {
//        pg.fill(0);
//        pg.text(pt.label, px + pw, py);  
//      }        
    }              
    pg.endDraw();
  }
  
  public Selection getSelection(double valx, double valy) {
    if (points.size() < 500 && !(varx.categorical() && vary.categorical())) {
      long nx = varx.getCount(ranges);
      long ny = vary.getCount(ranges);
//      /float rad = PApplet.map((float)count, 0, 1, 0.05f, 0.01f);
      int count = PApplet.min(500, points.size());
      float rad = PApplet.map(count, 0, 500, 0.05f, 0.01f);
      
      for (Value2D pt: points) {
        if (!varx.categorical() || !vary.categorical()) {
          float px, py;
          
          if (varx.categorical()) {
            long i = (long)(pt.x * (nx - 1));
            float dx = 1.0f / nx;
            px = dx/2 + dx * i;
          } else {
            px = (float)pt.x;  
          }
          
          if (vary.categorical()) {
            long j = (long)(pt.y * (ny-1));
            float dy = 1.0f / ny;
            py = 1 - (dy/2 + dy * j);          
          } else {
            py = 1 - (float)(pt.y);  
          }
          
//          System.out.println(px + " " + py + " " + rad);
          if (PApplet.dist((float)valx, (float)valy, px, py) < rad) {
//            System.err.println("found!");
            Selection sel = new Selection(px, py, rad, rad);
            sel.isEllipse = true;
            sel.setLabel(pt.label);
            return sel;            
          }
          
        }        
      }
      
      return null;
      
    } else {
      // TODO: needs some kind of tree representation of the data to search 
      // efficiently when there are many data points.      
      return null;      
    }    
  }  
  
  protected void initPoints(DataSlice2D slice) {
    points = new ArrayList<Value2D>();    
    if (varx.categorical() && vary.categorical()) {
      for (Value2D val: slice.values) {
        Value2D pt = search(val);
        if (pt == null) {
          points.add(new Value2D(val));
        } else {
          pt.w += val.w;
        }
      }
    } else {
      points.addAll(slice.values);  
    }
    weightSum = 0;
    for (Value2D val: slice.values) weightSum += val.w;    
  }
  
  protected Value2D search(Value2D val) {
    for (Value2D pt: points) {
      if (val.equals(pt)) return pt;     
    }
    return null;    
  }
}
