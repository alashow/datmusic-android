/*
 * Copyright (C) 2018, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.domain.errors

class EmptyResultException(override val message: String = "Result was empty") : RuntimeException(message)
