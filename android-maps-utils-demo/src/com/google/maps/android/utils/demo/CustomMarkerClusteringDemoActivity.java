/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.maps.android.utils.demo;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.MarkerManager.Collection;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.google.maps.android.utils.demo.model.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Demonstrates heavy customisation of the look of rendered clusters.
 */
public class CustomMarkerClusteringDemoActivity extends BaseDemoActivity implements ClusterManager.OnClusterClickListener<Person>, ClusterManager.OnClusterInfoWindowClickListener<Person>, ClusterManager.OnClusterItemClickListener<Person>, ClusterManager.OnClusterItemInfoWindowClickListener<Person> {
    private ClusterManager<Person> mClusterManager;
    private Random mRandom = new Random(1984);

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class PersonRenderer extends DefaultClusterRenderer<Person> {
        private final IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getApplicationContext());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public PersonRenderer() {
            super(getApplicationContext(), getMap(), mClusterManager);

            View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getApplicationContext());
            mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Person person, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            
        	if(person.urlbitmap==null){
        		mImageView.setImageResource(person.profilePhoto);        		
                Bitmap icon = mIconGenerator.makeIcon();
                
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
                
        		loadMarkerIcon(person);
        	}else{
        		mImageView.setImageBitmap(person.urlbitmap);
                Bitmap icon = mIconGenerator.makeIcon();
                
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(person.name);
        	}
            
        }
        
        @Override
        protected void onBeforeClusterRendered(Cluster<Person> cluster, MarkerOptions markerOptions) {
        	
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    
        
        private void loadMarkerIcon(final Person person) {
        	
        	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon");
        	        	
        	Glide.with(CustomMarkerClusteringDemoActivity.this).load(person.mUrl).asBitmap().fitCenter().into(new SimpleTarget<Bitmap>(){

        		@Override
        		public void onResourceReady(Bitmap bitmap,
        				GlideAnimation<? super Bitmap> glideanimation) {
        			Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" WxH="+bitmap.getWidth()+"x"+bitmap.getHeight());
        			
        			person.urlbitmap=bitmap;
        			
        			mImageView.setImageBitmap(person.urlbitmap);        			
                    Bitmap icon = mIconGenerator.makeIcon();
        			        			
        			PersonRenderer.this.getMarker(person).setIcon(BitmapDescriptorFactory.fromBitmap(icon));
        			
        		}
            	
            });
        }
        
        private void loadClusterIcon(final Person person, final Cluster<Person> cluster) {
        	
        	Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon");
        	        	
        	Glide.with(CustomMarkerClusteringDemoActivity.this).load(person.mUrl).asBitmap().fitCenter().into(new SimpleTarget<Bitmap>(){

        		@Override
        		public void onResourceReady(Bitmap bitmap,
        				GlideAnimation<? super Bitmap> glideanimation) {
        			Log.d("CustomMarkerClusteringDemoActivity", "loadMarkerIcon onResourceReady person.name="+person.name+" WxH="+bitmap.getWidth()+"x"+bitmap.getHeight());
        			
        			person.urlbitmap=bitmap;
        			
        			PersonRenderer.this.getMarker(cluster).setIcon(BitmapDescriptorFactory.fromBitmap(getClusterIcon(cluster)));
        			
        		}
            	
            });
        }
        
        private Bitmap getClusterIcon(Cluster<Person> cluster){
        	// Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Person p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                
                if(p.urlbitmap==null){
                	Drawable drawable = getResources().getDrawable(p.profilePhoto);
                	drawable.setBounds(0, 0, width, height);
                    profilePhotos.add(drawable);
                    
                    loadClusterIcon(p,cluster);
                }else{
                	BitmapDrawable bitmapdrawable = new BitmapDrawable(p.urlbitmap);
                	bitmapdrawable.setBounds(0, 0, width, height);
                    profilePhotos.add(bitmapdrawable);
                }
                                
                
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            
            return icon;
        }
                
    }

    @Override
    public boolean onClusterClick(Cluster<Person> cluster) {
        // Show a toast with some info when the cluster is clicked.
        String firstName = cluster.getItems().iterator().next().name;
        Toast.makeText(this, cluster.getSize() + " (including " + firstName + ")", Toast.LENGTH_SHORT).show();
        
        
        return true;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Person> cluster) {
        // Does nothing, but you could go to a list of the users.
    }

    @Override
    public boolean onClusterItemClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Person item) {
        // Does nothing, but you could go into the user's profile page, for example.
    }

    @Override
    protected void startDemo() {
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(51.503186, -0.126446), 9.5f));

        mClusterManager = new ClusterManager<Person>(this, getMap());
        mClusterManager.setRenderer(new PersonRenderer());
        getMap().setOnCameraChangeListener(mClusterManager);
        getMap().setOnMarkerClickListener(mClusterManager);
        getMap().setOnInfoWindowClickListener(mClusterManager);
        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterInfoWindowClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        addItems();
        mClusterManager.cluster();
    }

    private void addItems() {
        // http://www.flickr.com/photos/sdasmarchives/5036248203/
        mClusterManager.addItem(new Person(position(), "Walter-RunningMan", R.drawable.walter,"http://www.myiconfinder.com/uploads/iconsets/128-128-1bb4b317a4868bb52a12378fb5e8050d.png"));

        // http://www.flickr.com/photos/usnationalarchives/4726917149/
        mClusterManager.addItem(new Person(position(), "Gran-b", R.drawable.gran,"http://www.myiconfinder.com/uploads/iconsets/128-128-fe6760e3fce6f2a15a393c586306f297.png"));

        // http://www.flickr.com/photos/nypl/3111525394/
        mClusterManager.addItem(new Person(position(), "Ruth-Be", R.drawable.ruth,"http://www.myiconfinder.com/uploads/iconsets/128-128-e1e20c186ec020d053d1b76019dd4c7e.png"));

        // http://www.flickr.com/photos/smithsonian/2887433330/
        mClusterManager.addItem(new Person(position(), "Stefan-B", R.drawable.stefan,"http://www.myiconfinder.com/uploads/iconsets/128-128-4572fba0a74093691f6771c6818df713.png"));

        // http://www.flickr.com/photos/library_of_congress/2179915182/
        mClusterManager.addItem(new Person(position(), "Mechanic-GirlUnicorn", R.drawable.mechanic,"http://www.myiconfinder.com/uploads/iconsets/128-128-35dfada34dddb9f15e3e19760a6084bf.png"));

        // http://www.flickr.com/photos/nationalmediamuseum/7893552556/
        mClusterManager.addItem(new Person(position(), "Yeats-BMW", R.drawable.yeats,"http://www.myiconfinder.com/uploads/iconsets/128-128-1535181a892482c255d3447f54799bf2.png"));

        // http://www.flickr.com/photos/sdasmarchives/5036231225/
        mClusterManager.addItem(new Person(position(), "John-Velo", R.drawable.john,"http://www.myiconfinder.com/uploads/iconsets/128-128-3ce9c09a8d65db0b1ed01f365b6b52dd.png"));

        // http://www.flickr.com/photos/anmm_thecommons/7694202096/
        mClusterManager.addItem(new Person(position(), "Trevor the Turtle-Digg", R.drawable.turtle,"http://www.myiconfinder.com/uploads/iconsets/128-128-694878c3a6795e12e5b628140500137a.png"));

        // http://www.flickr.com/photos/usnationalarchives/4726892651/
        mClusterManager.addItem(new Person(position(), "Teach-Ball", R.drawable.teacher,"http://www.myiconfinder.com/uploads/iconsets/128-128-f8fe3b3147f688b59c972577137d6ae2.png"));
    }

    private LatLng position() {
        return new LatLng(random(51.6723432, 51.38494009999999), random(0.148271, -0.3514683));
    }

    private double random(double min, double max) {
        return mRandom.nextDouble() * (max - min) + min;
    }
}
