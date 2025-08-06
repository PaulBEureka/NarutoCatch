/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.narutocatch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.TimerTask;
import java.util.Timer;
import java.util.Random;
import javax.sound.sampled.*;

public class NarutoCatch extends JFrame {

  
    CustomCanvas DrawingArea;
    

    public NarutoCatch() {

        Container Pane;
        Pane = getContentPane();
        Pane.setLayout(null);
        DrawingArea = new CustomCanvas();
        DrawingArea.setBounds(0, 0, 500, 500);

        Pane.add(DrawingArea);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setSize(500, 500);
        show();
        

    }

    public static void main(String[] args) {
        NarutoCatch app = new NarutoCatch();
    }

}

class CustomCanvas extends Canvas {

    public int x;
    public int y;
    public int virtualX = 225;
    public int virtualY = 305;
    public int pos = 8;
    Timer timer;
    TimerTask task;

    Image imgCurent, heartImg, basketImg, bgImg;
    boolean moving = false;
    int gameAction, frame, index = 0,maxFruits = 5;
    String[] directions = {"L", "R"};
    Image[] img = new Image[8];
    Image[] fruits = new Image[4];
    Image[] spawnedFruits = new Image[maxFruits];
    ImageIcon icon;
    Rectangle  rect, sahigRect;   
    int randomCoorX, randomFruit, spawnCounter = 0, fruitCount = 0, frameCount = 0, score = 0, lives = 3;
    Random random = new Random();
    Rectangle[] recArr = new Rectangle[maxFruits];
    int[][] fruitCoor = new int[maxFruits][2];
    String[] bombCheckArr = new String[maxFruits];
    boolean isDead = false;
    int objFrameSpawn = 15;
    Clip clip;
    
    public CustomCanvas() {
        for (String direction : directions) {
                for (int f = 1; f <= 4; f++) {
                icon = new ImageIcon(direction + f + ".png");
                img[index] = icon.getImage();
                index++;
            } //loop frame to create all images in array form
            
        }
        
        index = 0;
        // create all fruit images in array form
        for (int i = 1; i < 5; i++){
            icon = new ImageIcon("f" + i +".png");
            fruits[index] = icon.getImage();
            index++;
        }
        
        icon = new ImageIcon("heart.png");
        heartImg = icon.getImage();
        
        icon = new ImageIcon("basket.png");
        basketImg = icon.getImage();
        
        icon = new ImageIcon("bg.png");
        bgImg = icon.getImage();
        
        imgCurent = img[0]; //initial image of naruto upon running code
        
        File bgMusicFile = new File("music.WAV");
        playMusic(bgMusicFile);
 
        timer = new Timer(true);
        task = new TimerTask() {
            @Override
            public void run() {
                moveIt();
                spawnIt(); //Added method to update fruit behaviour
            }
        };
        
        timer.schedule(task, 2000, 100);
        setBackground(Color.LIGHT_GRAY);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                gameAction = evt.getKeyCode();
                moving = true;
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                gameAction = 0;
                moving = false;
            }
        });

       

    }

   
    @Override
    public void paint(Graphics g) {

        super.paint(g);
        
        //Display background image
        g.drawImage(bgImg, 0, 40,getWidth(),getHeight()-180, this);
        
        //Loop through rec Array of fruits and redraw based on new coordinates
        for (int i = 0; i<maxFruits;i++){
            if (recArr[i] != null){
                recArr[i] = new Rectangle(fruitCoor[i][0], fruitCoor[i][1], 45,45);
                g.drawImage(spawnedFruits[i], fruitCoor[i][0], fruitCoor[i][1],45,45, this);
            }
        }
        g.setColor ( Color.BLACK );  
        
        //Display texts
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Help Naruto Catch 10 fruits while avoiding the bombs!!", 40 , 400);
        g.drawString("Fruits: " + score + " / 10", 50 , 30);
        g.drawString("Lives: "+lives + " / 3", 390 , 30);
        
        //Display floor, heart, and basket of fruits images.
        sahigRect = new Rectangle( 0 , 354,500, 15);   
        g.fillRect (sahigRect.x,sahigRect.y,sahigRect.width,sahigRect.height ); 
        g.drawImage(heartImg, 360, 12,25,25, this);
        g.drawImage(basketImg, 0, 3,50,50, this);
        
            
        //ito ang rect ni naruto
        rect = new Rectangle( virtualX , virtualY,imgCurent.getWidth(this)- 5, imgCurent.getHeight(this));       
        g.drawImage(imgCurent, virtualX, virtualY, this);
        
        //Check if the player collected 10 fruits, display win text, and stop timer
        if (score == 10){
                        g.setColor ( Color.BLACK );  
                        g.setFont(new Font("Arial", Font.BOLD, 36));
                        g.drawString("YOU WIN!!", 160 , 440);
                        timer.cancel();
                        timer.purge();
                        clip.stop();
                       
        }
        //Check if the player died, set the death boolean first to repaint dead naruto, and display lose text
        else if (lives == 0){  
            if (isDead == true){
            g.setColor ( Color.BLACK );  
                        g.setFont(new Font("Arial", Font.BOLD, 36));
                        g.drawString("YOU LOSE!!", 150 , 440);
                        timer.cancel();
                        timer.purge();
                        clip.stop();
            }
            else{
                icon = new ImageIcon("die.png");
                imgCurent = icon.getImage();
                isDead = true;
            }
        }
        
   
        Collide();

    }
    
    public void spawnIt(){
        //Spawn a fruit if the total number of fruits spawned is less than 5 and 20 frames past since last spawn
        if (fruitCount < 5 && spawnCounter == 20){
            //rect of fruits, x coordinates are random from 0 - 350
            randomCoorX = random.nextInt(351); 
            randomFruit = random.nextInt(4);
            
            for(int i = 0; i<maxFruits; i++){
                // Check which index is null and instantiate a rect, coordinates and store in on fruits spawned array
                if (recArr[i] == null){
                    recArr[i] = new Rectangle(randomCoorX, 30, 45,45);
                    fruitCoor[i][0] = randomCoorX;
                    fruitCoor[i][1] = 30;
                    spawnedFruits[i] = fruits[randomFruit];
                    //Identify if the spawned object is a bomb or fruit to identify collision effects
                    if (randomFruit == 3){
                        bombCheckArr[i] = "Bomb";
                    }
                    else{
                        bombCheckArr[i] = "Fruit";
                    }
                    break;
                }
            }
            spawnCounter = 0;
            fruitCount +=1;
        }
        
        // Add 50 to previous Y position of fruit every 15 frames
        if (frameCount == objFrameSpawn){
            for(int i = 0; i<maxFruits; i++){
                if(fruitCoor[i][0] != 0){
                    fruitCoor[i][1] += 50;
                }
            }
            frameCount = 0;
        }
        
        frameCount += 1;
        spawnCounter += 1;
        repaint();
    }
    

    public void moveIt() {
        if (moving == false) {
            return;
        }
        switch (gameAction) {
            
            case KeyEvent.VK_LEFT -> {
                if (virtualX > 10){
                    x = 3;
                    //movementleft();
                    virtualX-=5;
                    frame = (frame+1) % 2;
                    imgCurent = img[frame];
                }
                
            }
            case KeyEvent.VK_RIGHT -> {
                if (virtualX < 460)
                {
                    x = 4;
                    //  movementright();
                    virtualX+=5;
                    frame = (frame+1) % 2;
                    imgCurent = img[frame+4];
                }
                
            }
        }

    }

    
     public void Collide()    { 
       // Check if fruits collided with naruto or at the floor, then reset respective index value of collided object
       for (int i = 0; i<maxFruits; i++){
           if (recArr[i] != null){
               if (rect.intersects(recArr[i])){
                    if ("Bomb".equals(bombCheckArr[i])){
                       lives -= 1;  
                    }
                    else{
                       score += 1;
                       objFrameSpawn -=1;
                    }
                    fruitCoor[i][0] = 0;
                    fruitCoor[i][1] = 0;
                    recArr[i] = null;
                    spawnedFruits[i] = null;
                    fruitCount -= 1;
                    spawnCounter = 0; 
                }  
               else if (recArr[i].intersects(sahigRect)){
                    fruitCoor[i][0] = 0;
                    fruitCoor[i][1] = 0;
                    recArr[i] = null;
                    spawnedFruits[i] = null;
                    fruitCount -= 1;
                    spawnCounter = 0;
                }
           }
           
           
       }
       
       
    }
     
    //For background music
    public void playMusic(File Sound){
        try{
            clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(Sound));
            clip.start();
            
            
        }
        catch(IOException | LineUnavailableException | UnsupportedAudioFileException e){
            
        }
    }

}

