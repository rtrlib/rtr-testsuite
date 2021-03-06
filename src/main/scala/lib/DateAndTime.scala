/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package main.scala.lib

import org.joda.time._
import org.joda.time.format.PeriodFormat
import org.joda.time.format.DateTimeFormat
import java.util.Locale

object DateAndTime {
  def locale = new Locale("en", "UK")

  def dateTimeFormatter = DateTimeFormat.fullDateTime().withLocale(locale)
  def periodFormatter = PeriodFormat.getDefault.withLocale(locale)

  def formatDateTime(datetime: DateTime) = datetime.toString(dateTimeFormatter)

  def periodInWords(period: Period, number: Int = 2): String = periodFormatter.print(keepMostSignificantPeriodFields(period, number))

  def keepMostSignificantPeriodFields(period: Period, number: Int): Period = {
    val values = period.getValues
    val mostSignificantField = values.indexWhere(_ != 0)
    if (mostSignificantField < 0) {
      period
    } else {
      val result = new MutablePeriod()
      for (i <- mostSignificantField.until(mostSignificantField + number).intersect(values.indices)) {
        result.setValue(i, values(i))
      }
      result.toPeriod
    }
  }

  implicit object DateTimeOrdering extends Ordering[DateTime] {
    override def compare(x: DateTime, y: DateTime) = x.compareTo(y)
  }
}
