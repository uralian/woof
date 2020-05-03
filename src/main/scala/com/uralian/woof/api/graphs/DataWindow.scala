package com.uralian.woof.api.graphs

import com.uralian.woof.api.SortDirection

/**
 * Data window.
 *
 * @param order sort direction.
 * @param limit maximum number of items returned.
 */
final case class DataWindow(order: SortDirection, limit: Int)