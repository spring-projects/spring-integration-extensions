/*
 * Copyright 2002-2012 the original author or authors.
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
package org.springframework.integration.print.support;

import javax.print.attribute.standard.MediaTray;

import org.springframework.util.Assert;

/**
 * @author Gunnar Hillert
 * @since 1.0
 */
public enum MediaTrayEnum {

	BOTTOM(MediaTray.BOTTOM),
	ENVELOPE(MediaTray.ENVELOPE),
	LARGE_CAPACITY(MediaTray.LARGE_CAPACITY),
	MAIN(MediaTray.MAIN),
	MANUAL(MediaTray.MANUAL),
	MIDDLE(MediaTray.MIDDLE),
	SIDE(MediaTray.SIDE),
	TOP(MediaTray.TOP);

	private MediaTray mediaTray;

	private MediaTrayEnum(MediaTray mediaTray) {
		this.mediaTray = mediaTray;
	}

	public MediaTray getMediaTray() {
		return mediaTray;
	}

	public static MediaTray getForString(String mediaTrayAsString) {

		Assert.hasText(mediaTrayAsString, "'mediaTrayAsString' must neither be null nor empty.");

		final MediaTrayEnum[] mediaTrayEnumValues = MediaTrayEnum.values();

		for (MediaTrayEnum mediaTrayEnum : mediaTrayEnumValues) {
			if (mediaTrayEnum.name().equalsIgnoreCase(mediaTrayAsString)) {
				return mediaTrayEnum.getMediaTray();
			}
		}

		throw new IllegalArgumentException("Invalid mediaTray '" + mediaTrayAsString
				+ "' provided.");

	}
}
