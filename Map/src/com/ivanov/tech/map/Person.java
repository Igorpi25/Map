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

package com.ivanov.tech.map;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

public class Person implements ClusterItem {
    public final String name;
    
    public int ic_missing;
    public String url_icon;
    public int user_id;
    public float accuracy;
    public long timestamp;
    public int provider;
    
    public LatLng position;
    public LatLng lastmarkerposition;
    
    public Bitmap urlbitmap =null;
    
    public Person(int user_id, LatLng position, float accuracy, long timestamp, String name, int pictureResource,String url) {
        this.name = name;
        this.ic_missing = pictureResource;
        this.position = position;   
        this.accuracy=accuracy;
        this.timestamp=timestamp;
        this.url_icon=url;
        this.user_id=user_id;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }
    
    
    
}
