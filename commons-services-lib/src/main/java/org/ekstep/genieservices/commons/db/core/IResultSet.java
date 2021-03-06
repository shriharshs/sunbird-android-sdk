package org.ekstep.genieservices.commons.db.core;

/**
 * Created on 17/4/17.
 *
 * @author swayangjit
 */
public interface IResultSet {
    /**
     * Returns the numbers of rows in the cursor.
     *
     * @return the number of rows in the cursor.
     */
    int getCount();

    /**
     * Returns the current position of the cursor in the row set.
     * The value is zero-based. When the row set is first returned the cursor
     * will be at positon -1, which is before the first row. After the
     * last row is returned another call to next() will leave the cursor past
     * the last entry, at a position of count().
     *
     * @return the current cursor position.
     */
    int getPosition();

    /**
     * Move the cursor to the first row.
     *
     * @return whether the move succeeded.
     */
    boolean moveToFirst();

    /**
     * Move the cursor to the next row.
     * <p>
     * <p>This method will return false if the cursor is already past the
     * last entry in the result set.
     *
     * @return whether the move succeeded.
     */
    boolean moveToNext();

    /**
     * Returns the zero-based index for the given column name, or -1 if the column doesn't exist.
     *
     * @param columnName the name of the target column.
     * @return the zero-based column index for the given column name, or -1 if
     * the column name does not exist.
     */
    int getColumnIndex(String columnName);

    /**
     * Returns the zero-based index for the given column name, or throws
     *
     * @param columnName the name of the target column.
     * @return the zero-based column index for the given column name
     * @throws IllegalArgumentException if the column does not exist
     */
    int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException;

    /**
     * Returns the column name at the given zero-based column index.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the column name for the given column index.
     */
    String getColumnName(int columnIndex);

    /**
     * Returns a string array holding the names of all of the columns in the
     * result set in the order in which they were listed in the result.
     *
     * @return the names of the columns returned in this query.
     */
    String[] getColumnNames();

    /**
     * Return total number of columns
     *
     * @return number of columns
     */
    int getColumnCount();


    /**
     * Returns the value of the requested column as a String.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a String.
     */
    String getString(int columnIndex);

    /**
     * Returns the value of the requested column as an int.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as an int.
     */
    int getInt(int columnIndex);

    /**
     * Returns the value of the requested column as a long.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a long.
     */
    long getLong(int columnIndex);

    /**
     * Returns the value of the requested column as a double.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a double.
     */
    double getDouble(int columnIndex);

    /**
     * Returns the value of the requested column as a byte array.
     * <p>
     * <p>The result and whether this method throws an exception when the
     * column value is null or the column type is not a blob type is
     * implementation-defined.
     *
     * @param columnIndex the zero-based index of the target column.
     * @return the value of that column as a byte array.
     */
    byte[] getBlob(int columnIndex);

    /**
     * Returns whether the cursor is pointing to the position after the last
     * row.
     *
     * @return whether the cursor is after the last result.
     */
    boolean isAfterLast();


}
