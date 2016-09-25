/*
 * Copyright 2014. Alashov Berkeli
 *
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tm.alashow.datmusic.util;

/**
 * Created by alashov on 24/09/2016.
 */

public class ArrayUtils {

    public static boolean contains(Integer[] array, int valueToFind) {
        return indexOf(array, valueToFind, 0) != - 1;
    }

    public static int indexOf(Integer[] array, int valueToFind, int startIndex) {
        if (array == null) {
            return - 1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for(int i = startIndex; i < array.length; ++ i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }
            return - 1;
        }
    }
}
