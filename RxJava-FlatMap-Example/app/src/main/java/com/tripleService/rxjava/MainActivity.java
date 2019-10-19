package com.tripleService.rxjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import android.os.Bundle;


import com.tripleService.rxjava.models.Comment;
import com.tripleService.rxjava.models.Post;
import com.tripleService.rxjava.requests.ServiceGenerator;

import java.util.List;


public class MainActivity extends AppCompatActivity implements Observer<Post> {

    private static final String TAG = "MainActivity";

    //ui
    private RecyclerView recyclerView;

    // vars
    private CompositeDisposable disposables = new CompositeDisposable();
    private RecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);

        initRecyclerView();


        final Observable<Post> postsObservable = ServiceGenerator.getRequestApi().getPosts().
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                flatMap(new Function<List<Post>, ObservableSource<Post>>() {
                    @Override
                    public ObservableSource<Post> apply(List<Post> posts) throws Exception {
                        adapter.setPosts(posts);
                      return   Observable.fromIterable(posts);
                    }
                });

        postsObservable.subscribeOn(Schedulers.io()).
                flatMap(new Function<Post, ObservableSource<Post>>() {
                    @Override
                    public ObservableSource<Post> apply(final Post post) throws Exception {
                        return getComments(post);
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    private ObservableSource<Post> getComments(final Post post) {
       return ServiceGenerator.getRequestApi().getComments(post.getId()).
                map(new Function<List<Comment>, Post>() {
                    @Override
                    public Post apply(List<Comment> comments) throws Exception {
                        post.setComments(comments);
                        return post;
                    }
                }).subscribeOn(Schedulers.io());
    }

    private void initRecyclerView() {
        adapter = new RecyclerAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(Post post) {
        adapter.updatePost(post);
    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onComplete() {

    }
}
