/*
 * Copyright (c) 2014-2019, Bolotin Dmitry, Chudakov Dmitry, Shugay Mikhail
 * (here and after addressed as Inventors)
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mixcr.alleles

import com.fasterxml.jackson.core.type.TypeReference
import com.milaboratory.util.GlobalObjectMappers

object FindAllelesParametersPresets {
    private var knownParameters: Map<String, FindAllelesParameters>? = null
    private fun ensureInitialized() {
        if (knownParameters == null) synchronized(FindAllelesParametersPresets::class.java) {
            if (knownParameters == null) {
                val `is` =
                    FindAllelesParameters::class.java.classLoader.getResourceAsStream("parameters/find_alleles_parameters.json")
                val typeRef: TypeReference<HashMap<String, FindAllelesParameters>> =
                    object : TypeReference<HashMap<String, FindAllelesParameters>>() {}
                knownParameters = GlobalObjectMappers.ONE_LINE.readValue(`is`, typeRef)
            }
        }
    }

    val availableParameterNames: Set<String>
        get() {
            ensureInitialized()
            return knownParameters!!.keys
        }

    @JvmStatic
    fun getByName(name: String): FindAllelesParameters? {
        ensureInitialized()
        val params = knownParameters!![name] ?: return null
        return params.copy()
    }
}