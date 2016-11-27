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

package com.google.maps.android.utils.demo.model;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Person implements ClusterItem {
    public final String name;
    public final int profilePhoto;
    public final String mUrl;
    private final LatLng mPosition;
    public Bitmap urlbitmap =null; 
    

    public Person(LatLng position, String name, int pictureResource,String url) {
        this.name = name;
        profilePhoto = pictureResource;
        mPosition = position;
        mUrl=url;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }
    
}
