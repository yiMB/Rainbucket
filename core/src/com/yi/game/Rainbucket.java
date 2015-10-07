package com.yi.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class Rainbucket extends ApplicationAdapter {
	/**
	 * contains image droplet.png
	 */
	private Texture textureDroplet;
	/**
	 * contains image bucket.png
	 */
	private Texture textureBucket;
	/**
	 * contain short sound affect drop.wav
	 */
	private Sound soundDrop;
	/**
	 * contain long music rain.mp3
	 */
	private Music musicRain;
	/**
	 * use to draw object
	 */
	private SpriteBatch spriteBatch;
	/**
	 * use as boundary for collision detection,
	 * logically representing the image bucket.png
	 */
	private Rectangle rectangleBucket;
	/**
	 * Array of Rectangle,
	 * logically representing all images of droplet.png
	 */
	private Array<Rectangle> arrayDroplets;
	/**
	 * last timestamp of created a droplet
	 */
	private long lastDropletTime;

	/**
	 * phone screen size, and 2 image sizes
	 */
	float screenWidth, screenHeight, dropletImageWidth, dropletImageHeight, bucketImageWidth, bucketImageHeight;

	/**
	 * create() method use to setup the game initially,
	 * create() method only call once when the game first start
	 */
	@Override
	public void create() {
		/*
		define or load the image by
		new Texture(Gdx.files.internal("file")
			for android the location of these resource files is in assets folder inside android folder
		 */
		textureDroplet = new Texture(Gdx.files.internal("droplet.png"));
		textureBucket = new Texture(Gdx.files.internal("bucket.png"));

		/*
		define the sound and music by
		Gdx.audio.newSound(Gdx.files.internal("file")
		Gdx.audio.newMusic(Gdx.files.internal("file")
		 */
		soundDrop = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		musicRain = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		/*
		get the phone screen size, and 2 image sizes by
		Gdx.graphics
			getWidth()
			getHeight()
		Texture
			getWidth()
			getHeight()
		 */
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		dropletImageWidth = textureDroplet.getWidth();
		dropletImageHeight = textureDroplet.getHeight();
		bucketImageWidth = textureBucket.getWidth();
		bucketImageHeight = textureBucket.getHeight();

		/*
		print out to logcat,
		print the phone screen size, and 2 image sizes
		 */
		Gdx.app.log("MyTag", "screenWidth = " + screenWidth);
		Gdx.app.log("MyTag", "screenHeight = " + screenHeight);
		Gdx.app.log("MyTag", "dropImageWidth = " + dropletImageWidth);
		Gdx.app.log("MyTag", "dropImageHeight = " + dropletImageHeight);
		Gdx.app.log("MyTag", "bucketImageWidth = " + bucketImageWidth);
		Gdx.app.log("MyTag", "bucketImageHeight = " + bucketImageHeight);

		// set when music end keep playback the music
		musicRain.setLooping(true);
		// start to play the music
		musicRain.play();

		// create the SpriteBatch
		spriteBatch = new SpriteBatch();

		// create a Rectangle to logically represent the bucket
		rectangleBucket = new Rectangle();
		rectangleBucket.x = screenWidth / 2 - bucketImageWidth / 2; // center the bucket horizontally
		rectangleBucket.y = 20; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
		rectangleBucket.width = bucketImageWidth;
		rectangleBucket.height = bucketImageHeight;

		// create the raindrops array and spawn the first raindrop
		arrayDroplets = new Array<Rectangle>();
		generateDroplet();
	}

	/**
	 * generate a Rectangle logically representing 1 droplet, add to array, track current timestamp
	 */
	private void generateDroplet() {
		Rectangle rectangleDroplet = new Rectangle();
		rectangleDroplet.x = MathUtils.random(0, screenWidth - dropletImageWidth);
		rectangleDroplet.y = screenHeight;
		rectangleDroplet.width = dropletImageWidth;
		rectangleDroplet.height = dropletImageHeight;
		arrayDroplets.add(rectangleDroplet);
		lastDropletTime = TimeUtils.nanoTime();
	}

	/**
	 * render() method does all the updating and drawing,
	 * render() method being call all the time after the game started, may be 1000 time per second
	 */
	@Override
	public void render() {
		/*
		set clear screen with a black color. The arguments to glClearColor are the red, green,
		blue, and alpha component in the range [0,1] of the color to be used to clear the screen.
		 */
		Gdx.gl.glClearColor(0, 0, 0, 1);
		/*
		actually execute clear screen
		 */
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		/*
		begin()
			begin the batch to draw the bucket and all droplets
		end()
			other bach will have to wait for this batch to finish
		 */
		spriteBatch.begin();
		spriteBatch.draw(textureBucket, rectangleBucket.x, rectangleBucket.y);
		for(Rectangle raindrop: arrayDroplets) {
			spriteBatch.draw(textureDroplet, raindrop.x, raindrop.y);
		}
		spriteBatch.end();

		/*
		process user input for android touch
		Gdx.input.isTouched()	if screen got touch, like drag
			Gdx.input.justTouched()		if a new touch just occurred
		Gdx.input.getX()		touch position x of screen
		Gdx.input.getY()		touch position y of screen

		change Rectangle bucket position to the touch position
		 */
		if(Gdx.input.isTouched()) {
			rectangleBucket.x = Gdx.input.getX() - bucketImageWidth / 2;
		}

		// make sure the bucket stays within the screen bounds
		if(rectangleBucket.x < 0) {
			rectangleBucket.x = 0;
		}
		if(rectangleBucket.x > screenWidth - bucketImageWidth){
			rectangleBucket.x = screenWidth - bucketImageWidth;
		}

		/*
		create new droplet every 1 second
		check if we need to create a new droplet
		 */
		if(TimeUtils.nanoTime() - lastDropletTime > 1000000000){
			generateDroplet();
		}

		/*
		move droplet, remove droplet pass bottom of screen or hit the bucket,
		if droplet hit the bucket also play a sound effect
		 */
		for(int i=0; i < arrayDroplets.size; i++){
			arrayDroplets.get(i).y -= 200 * Gdx.graphics.getDeltaTime();
			if(arrayDroplets.get(i).y + dropletImageHeight < 0){
				arrayDroplets.removeIndex(i);
			}
			if(arrayDroplets.get(i).overlaps(rectangleBucket)){
				soundDrop.play();
				arrayDroplets.removeIndex(i);
			}
		}
	}

	/**
	 * dispose objects you created automatically when no longer needed,
	 * if the objects you created can call method dispose() then it can be dispose
	 */
	@Override
	public void dispose() {
		textureDroplet.dispose();
		textureBucket.dispose();
		soundDrop.dispose();
		musicRain.dispose();
		spriteBatch.dispose();
	}
}
