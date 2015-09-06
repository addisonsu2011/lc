package com.sumugu.liubo.lc.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.sumugu.liubo.lc.contract.*;

/**
 * Created by liubo on 15/9/1.
 */
public class ItemProvider extends ContentProvider {

    private static final String TAG=ItemProvider.class.getSimpleName();
    private DbHelper mDbHelper;

    //URI ƥ����
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        sUriMatcher.addURI(ItemContract.AUTHORITY,ItemContract.TABLE,ItemContract.ITEM_DIR);    //������
        sUriMatcher.addURI(ItemContract.AUTHORITY,ItemContract.TABLE+"/#",ItemContract.ITEM_ITEM); //һ�����
    }

    @Override
    public boolean onCreate() {

        //��ʼ����Ա����
        mDbHelper = new DbHelper(getContext());
        Log.d(TAG,"sumugu,onCreate,mDbHelper.");
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(ItemContract.TABLE);

        switch (sUriMatcher.match(uri))
        {
            case ItemContract.ITEM_DIR:
                break;
            case ItemContract.ITEM_ITEM:
                queryBuilder.appendWhere(ItemContract.Column.ITEM_ID +"="+uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("sumugu,Illegal URI:"+uri);
        }

        String orderBy = (TextUtils.isEmpty(sortOrder))?ItemContract.DEFAULT_SORT:sortOrder;
        //��ѯ���ѣ����Դ�ֻ�����ݿ�
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();
        //��ѯ����������α�
        Cursor cursor = queryBuilder.query(sqLiteDatabase,projection,selection,selectionArgs,null,null,orderBy);

        //����uri��ע����
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        Log.d(TAG,"sumugu,queried records:"+cursor.getCount());
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri))
        {
            case ItemContract.ITEM_DIR:
                Log.d(TAG,"sumugu,gotType:"+ItemContract.ITEM_TYPE_DIR);
                return ItemContract.ITEM_TYPE_DIR;
            case ItemContract.ITEM_ITEM:
                Log.d(TAG,"sumugu,gotType:"+ItemContract.ITEM_ITEM);
                return ItemContract.ITEM_TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Illegal URI:"+uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri uriRet=null;

        //�ж���ȷ��Uri��������CONTENT_URI,����ָ��ID,����DIR
        if(sUriMatcher.match(uri)!=ItemContract.ITEM_DIR){
            throw new IllegalArgumentException("sumugu,Illegal uri:"+uri);
        }

        //��Ϊ�ǲ��붯������Ҫ��д�����ݿ�
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

        //��ʼ���룬��ȡ���ص�ID�������жϳɹ���-1����ʧ��
        long rowId=sqLiteDatabase.insertWithOnConflict(ItemContract.TABLE,null,values,SQLiteDatabase.CONFLICT_IGNORE);

        if (rowId!=-1)
        {
            long id=values.getAsLong(ItemContract.Column.ITEM_ID);
            uriRet = ContentUris.withAppendedId(uri,id);
            Log.d(TAG,"sumugu,inserted uri:"+uriRet);

            //֪ͨ�������uri�������Ѿ�����
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return uriRet;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int ret=0;
        String where;
        switch (sUriMatcher.match(uri))
        {
            case ItemContract.ITEM_DIR:
                where = (null == selection)?"1":selection;  //ɾ��ȫ������������
                break;
            case ItemContract.ITEM_ITEM:
                long id= ContentUris.parseId(uri);  //��ȡuri��D���֣�����ID��ɾ��ָ��ID����Ŀ
                where = ItemContract.Column.ITEM_ID+"="+id
                        +(TextUtils.isEmpty(selection)?"":" and ( "+selection+" )");
                break;
            default:
                throw new IllegalArgumentException("sumugu,Illegal URI:"+uri);
        }

        //Ҫɾ����һ����Ҫ��д�����ݿ�ʵ��
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        //��ʼɾ�������ؽ����ɾ����Ŀ��������
        ret = sqLiteDatabase.delete(ItemContract.TABLE,where,selectionArgs);
        if(ret>0)
        {
            //�ɹ�ɾ����֪ͨʹ�����uri�������Ѿ�����
            getContext().getContentResolver().notifyChange(uri,null);
        }
        Log.d(TAG,"sumugu,deleted records:"+ret);
        return ret;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String where;

        switch(sUriMatcher.match(uri)){
            case ItemContract.ITEM_DIR:
                //��ָ��id���������Ҫ�����µ�����
                where = selection;
                break;
            case ItemContract.ITEM_ITEM:
                long id= ContentUris.parseId(uri);
                where = ItemContract.Column.ITEM_ID + "=" + id
                        +(TextUtils.isEmpty(selection) ? "":" and ("
                        + selection +")");
                break;
            default:
                throw new IllegalArgumentException("Illegal uri:"+uri);
        }

        //����Ҫ�޸����ݿ⣬����Ҫ��д�����ݿ�ʵ��
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        //�����޸���Ŀ������
        int ret = sqLiteDatabase.update(ItemContract.TABLE, values, where, selectionArgs);

        if(ret>0){
            //֪ͨuri�����Ѿ�����
            getContext().getContentResolver().notifyChange(uri, null);
        }
        Log.d(TAG,"sumugu,updated records:"+ret);
        return ret;
    }
}
