package com.example.android.notepad;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class NoteSearch extends Activity implements SearchView.OnQueryTextListener
{
    ListView listView;
    SQLiteDatabase sqLiteDatabase;
    private static final String[] PROJECTION = new String[]{
            NotePad.Notes._ID, // 0
            NotePad.Notes.COLUMN_NAME_TITLE, // 1
            NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE//时间
    };
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(this, "您选择的是："+query, Toast.LENGTH_SHORT).show();
        return false;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_search);
        SearchView searchView = (SearchView) findViewById(R.id.search);
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(NotePad.Notes.CONTENT_URI);
        }
        listView = (ListView) findViewById(R.id.list);
        sqLiteDatabase = new NotePadProvider.DatabaseHelper(this).getReadableDatabase();
        //设置该SearchView显示搜索按钮
        searchView.setSubmitButtonEnabled(true);
        //设置该SearchView内默认显示的提示文本
        searchView.setQueryHint("查找");
        searchView.setOnQueryTextListener(this);

    }
    public boolean onQueryTextChange(String string) {
        // 查询条件：标题或笔记内容包含输入的字符串
        String selection1 = NotePad.Notes.COLUMN_NAME_TITLE + " like ? or " + NotePad.Notes.COLUMN_NAME_NOTE + " like ?";
        String[] selection2 = {"%" + string + "%", "%" + string + "%"};

        // 执行查询操作
        Cursor cursor = sqLiteDatabase.query(
                NotePad.Notes.TABLE_NAME,   // 查询的表名
                PROJECTION,                 // 返回查询结果的列
                selection1,                 // 查询条件
                selection2,                 // 查询条件对应的值
                null,                       // 不对查询结果进行分组
                null,                       // 不对查询结果进行行组过滤
                NotePad.Notes.DEFAULT_SORT_ORDER // 默认的排序方式
        );

        // 定义要显示的游标列名，这里初始化为标题列
        String[] dataColumns = {
                NotePad.Notes.COLUMN_NAME_TITLE,    // 标题列
                NotePad.Notes.COLUMN_NAME_MODIFICATION_DATE  // 修改日期列
        };

        // 定义显示游标列的视图ID，这里初始化为noteslist_item.xml中的TextView
        int[] viewIDs = {
                android.R.id.text1,  // 显示标题的TextView
                android.R.id.text2   // 显示修改日期的TextView
        };

        // 创建适配器，将游标数据绑定到ListView
        SimpleCursorAdapter adapter
                = new SimpleCursorAdapter(
                this,                             // 上下文，ListView的父类
                R.layout.noteslist_item,         // 指定列表项的布局文件
                cursor,                          // 查询结果游标
                dataColumns,                     // 显示的数据列
                viewIDs                          // 对应视图ID
        );

        // 设置ListView的适配器为刚刚创建的CursorAdapter
        listView.setAdapter(adapter);

        return true;
    }
}