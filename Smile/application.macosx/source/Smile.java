import processing.core.*; 
import processing.data.*; 
import processing.opengl.*; 

import controlP5.*; 
import ddf.minim.*; 
import java.util.ArrayList; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class Smile extends PApplet {

/*
 
 Smile audio level detector.
 
 Copyright (C) 2013  Henry Hammond
    email: HenryHHammond92@gmail.com
           HenryHammond@cmail.carleton.ca
 
  This application uses the primary audio source of a computer to get audio,
  then using the user's choice of graphics and audio theshold will alert the
  user whenever the audio levels are above the desired threshold.
  
  Since not all microphones and rooms are made equal, the slider is used to 
  customize and regulate the room audio theshold level.
  
  The user has a choice of graphics to choose from through the dropdown box.
  
  This application uses the ControlP5 and Mimim libraries which are licensed under the
  GNU Lesser GPL license.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  For a copy of the GNU Lesser General Public License, see
  <http://www.gnu.org/licenses/>.
 
 */






//audio classes to be used
Minim minim;
AudioInput input;

//GUI kit to be used
ControlP5 cp5;

//max and min audio levels
float maxLevel = 0.1f;
float minLevel = 0.0f;

//frames per second
int fps = 30;

//Arraylist to hold previous audio states and reduce 
//noise and random variations during analysis
ArrayList<Float> shortHistory;
int shortLength = (int)(fps*0.5f);

//dropdown list to choose which design to follow
DropdownList design;

//lock controls on or off
boolean lock = false;


//The initialization code
public void setup() {
  size(800, 600);  // size always goes first!

  frameRate(fps);
  
  //allow this app to be resized
  if (frame != null) {
    frame.setResizable(true);
  }
  
  //prepare audio tools
  minim = new Minim(this);
  input = minim.getLineIn(Minim.MONO, 512);
  
  //prepare history
  shortHistory = new ArrayList();
  
  //prpare graphics
  ellipseMode(CENTER);

  //create GUI elements
  cp5 = new ControlP5(this);
  
  //set colours for our GUI elements
  cp5.setColorForeground(color(160, 160, 160));
  cp5.setColorBackground(color(200, 200, 200));
  cp5.setColorLabel(color(0, 0, 0));
  cp5.setColorValue(color(0, 0, 0));
  cp5.setColorActive(color(21, 115, 255));
  
  //add audio threshold slider
  cp5.addSlider("maxLevel")
    .setPosition(5, 5)
      .setSize(150, 10)
        .setRange(0, 1)
          .setValue(0.01f);
  
  //add dropdown to choose design
  design = cp5.addDropdownList("design")
    .setPosition(5, 30)
      .setSize(150, 150)
        .setBarHeight(10)
          .setItemHeight(15);
  //add elements to drop down
  design.captionLabel().set("Smile");
  design.addItem("Smile", 0);
  design.addItem("Traffic Light", 1);
  
  //craete label for info about spacebar
  cp5.addTextlabel("label")
  .setText("Press space to hide and show options")
  .setPosition(5,35)
  .setColorValue(color(140));
  
}

public void draw() {
  
  //set colour mode to RGB for background
  colorMode(RGB, 255);
  background(255);
  
  //get audio input levels
  float level = input.left.level();
  
  //update history
  shortHistory.add(level);
  if (shortHistory.size() > shortLength) {
    shortHistory.remove(0);
  }
  
  //calculate mean from history
  float shortMean = 0;
  for (int i=0;i<shortHistory.size();i++) {
    shortMean += shortHistory.get(i);
  }
  shortMean/=shortHistory.size();
  
  //set value of happiness with a mapping between 0 and 1
  float happiness = exp(-shortMean/maxLevel);
  
  //draw elements based on dropdown value
  if ( design.getValue() == 0) {
    drawFace(happiness);
  }
  else if (design.getValue() == 1) { 
    drawLight(happiness);
  }
}


//Draw Traffic Light design
public void drawLight(float happiness) {
  
  
  //initial colour and draw settings
  colorMode(RGB);
  fill(255, 255, 0);
  stroke(0);
  strokeWeight(2);

  //draw light
  int dim = min(width, height)-40;
  
  //set appropriate width and height of light
  int w = dim*9/16;
  int h = dim;
  
  //set offsets to make position easier
  int offsetX = (width-w)/2;
  int offsetY = (height-h)/2;
  
  //draw traffic light background
  rect( (width-w)/2, (height-h)/2, w, h);
  
  //These values are used to determine which colour to make the light
  final int RED = 3;
  final int YELLOW = 2;
  final int GREEN = 1;

  //map happiness to values between 0 and 4
  happiness =4-4*happiness;
  
  //light radious
  int rad = PApplet.parseInt(h*1.0f/3);

  //draw red light
  fill( happiness>YELLOW ? color(255, 0, 0):color(98, 56, 53));
  ellipse( offsetX+PApplet.parseInt(w*1.0f/2), offsetY+PApplet.parseInt(h*1.0f/3)-rad/2, rad-10, rad-10);

  //yellow
  fill( happiness<YELLOW && happiness>GREEN ? color(255, 255, 0):color(61, 61, 41));
  ellipse( offsetX+PApplet.parseInt(w*1.0f/2), offsetY+PApplet.parseInt(h*2.0f/3)-rad/2, rad-10, rad-10);

  //green
  fill( happiness<=GREEN ? color(18, 170, 64):color(61, 61, 41));
  ellipse( offsetX+PApplet.parseInt(w*1.0f/2), offsetY+PApplet.parseInt(h*3.0f/3)-rad/2, rad-10, rad-10);
}

public void drawFace(float happiness) {
  
  //set colour and draw settings
  stroke(0);
  strokeWeight(2);
  colorMode(HSB, 360, 255, 255, 255);
  
  //settings to draw happy face the rigth colours
  float topHue = 115;  //greenvalue
  float bottomHue = 212;  //blue value
  //we use sqrt to make the change in colours smoother and nicer
  float hueLevel = ((topHue+360-bottomHue)*sqrt(happiness)+bottomHue)%360;
  
  fill( hueLevel, 255, 200);
  
  //dimensions of happyface
  float dim = min(width, height);

  float faceRadius = 8*dim/10;

  float faceX = dim/2*max(width, height)/min(width, height);
  float faceY = dim/2;

  //draw face
  ellipse(faceX, faceY, faceRadius, faceRadius);  


  //draw eyes
  fill( hueLevel, 240, 0);
  float eyeRadius = faceRadius*0.08f;
  float eyeOffsetX = faceRadius*0.15f;
  float eyeOffsetY = faceRadius*0.1f;
  ellipse( faceX-eyeOffsetX, faceY-eyeOffsetY, eyeRadius, eyeRadius);
  ellipse( faceX+eyeOffsetX, faceY-eyeOffsetY, eyeRadius, eyeRadius);

  //draw mouth
  float mouthHeight = faceRadius*.12f;
  float mouthWidth = faceRadius*.4f;
  float mouthOffset = faceRadius*0.25f;
  
  noFill();
  
  bezier( faceX-mouthWidth/2, faceY+mouthOffset+mouthHeight*(0.5f-happiness), 
  faceX-mouthWidth*0.25f, faceY+mouthOffset+mouthHeight*(happiness-0.5f)*1.1f, 
  faceX+mouthWidth*0.25f, faceY+mouthOffset+mouthHeight*(happiness-0.5f)*1.1f, 
  faceX+mouthWidth/2, faceY+mouthOffset+mouthHeight*(0.5f-happiness)
    );
}

public void keyPressed() {
  //if spacebar pressed hide GUI elements
  if (key ==' ') {
    lock = !lock;
    if (lock) {
      cp5.hide();
    }
    else {
      cp5.show();
    }
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Smile" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
