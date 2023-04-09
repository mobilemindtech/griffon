/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2008-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.swing.converters.spi;

import griffon.converter.Converter;
import griffon.converter.spi.ConverterProvider;
import griffon.swing.converters.ImageConverter;
import org.kordamp.jipsy.annotations.ServiceProviderFor;

import java.awt.*;

/**
 * @author Andres Almiray
 * @since 3.0.0
 */
@ServiceProviderFor(ConverterProvider.class)
public class ImageConverterProvider implements ConverterProvider<Image> {
    @Override
    public Class<Image> getTargetType() {
        return Image.class;
    }

    @Override
    public Class<? extends Converter<Image>> getConverterType() {
        return ImageConverter.class;
    }
}
