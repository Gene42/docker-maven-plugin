/*
 * Copyright (c) 2014 Spotify AB.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.docker;

import org.apache.maven.plugins.annotations.Mojo;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ImageNotFoundException;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.RemovedImage;
import com.spotify.docker.client.shaded.javax.ws.rs.NotFoundException;

/**
 * Removes a dangling docker images. The ones with name and tag <none>
 * @version $Id$
 */
@Mojo(name = "removeDanglingImages")
public class RemoveDanglingImagesMojo extends AbstractDockerMojo {

    @Override
    protected void execute(final DockerClient docker)
        throws DockerException, InterruptedException {
        for (final Image image : docker.listImages(
            DockerClient.ListImagesParam.danglingImages(true))) {
            try {
                // force the image to be removed but don't remove untagged parents
                for (final RemovedImage removedImage : docker.removeImage(
                    image.id(), true, false)) {
                    getLog().info("Removed: " + removedImage.imageId());
                }
            } catch (ImageNotFoundException | NotFoundException e) {
                // ignoring 404 errors only
                getLog().warn("Image " + image
                    + " doesn't exist and cannot be deleted - ignoring", e);
            }
        }
    }
}
