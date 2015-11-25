package org.dataconservancy.packaging.tool.impl;

import org.junit.experimental.categories.Category;

/**
 * Marker interface for tests that may require escalated privileges on certain
 * hosts.
 * <p>
 * Certain operations such as <a href=
 * "https://social.msdn.microsoft.com/Forums/en-US/windowssdk/thread/fa504848-a5ea-4e84-99b7-0eb4e469cbef?outputAs=rss">
 * symbolic linking</a> may be disabled by default some operating systems. Junit
 * tests marked with a {@link Category} of Privileged may require such
 * privileges.
 * </p>
 * 
 * @author apb
 *
 */
public interface Privileged {

}
