package io.github.ackeecz.apythia.http.util

internal class MutualExclusivityChecker<T : MutualExclusivityChecker.Group> {

    private var activeGroup: T? = null

    fun checkGroup(group: T) {
        if (activeGroup == null) {
            activeGroup = group
        } else if (activeGroup != group) {
            error("Cannot use ${group.groupName} after using ${checkNotNull(activeGroup).groupName}")
        }
    }

    /**
     * Group of operations that can be used together. Groups are exclusive, meaning that once a
     * particular group is used, you cannot use any other group.
     */
    interface Group {

        /**
         * Name of the group that is used for error messages.
         */
        val groupName: String
    }
}
