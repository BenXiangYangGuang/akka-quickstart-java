package com.wewe;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.typesafe.config.ConfigFactory;

import static com.wewe.TransformationMessages.BACKEND_REGISTRATION;

/**
 * @Author: fei2
 * @Date: 18-7-9 上午11:17
 * @Description:
 *
 * 目前这个集群，服务器端和服务器端之间不能进行通信；客户端和客户端不能进行通信；客户端维持着自己所联系的所有服务器；
 * 当有服务器上线时，服务器收到所有集群节点信息ClusterEvent.CurrentClusterState；找出其中的所有客户端；向客户端发送消息注册自己；
 * 当客户端上线时，服务器端接受到ClusterEvent.MemberUp，向客户端发送消息，注册服务器到客户端
 * 服务发现与维护
 * 上面代码已经有注释了，有2点：
    - 有新节点加入时，如果是服务器端角色，则像客户端注册自己的信息。客户端收到消息以后会讲这个服务端存到本机服务列表中
    - 服务端当前节点在刚刚加入集群时，会收到CurrentClusterState消息，从中可以解析出集群中的所有前端节点（即roles为frontend的），并向其发送BACKEND_REGISTRATION消息，用于注册自己
 *
 * 1.新增客户端
 *      各个服务器段代码MyAkkaClusterServer正在运行;监听新增节点ClusterEvent.MemberUp的状态,各自服务器端根据getContext()得到当前客户端;并向次客户端发送注册服务类型的消息;
 *      客户端接受消息;添加相应的服务器端到自己的注册容器中.
 * 2.新增服务器端
 *      根据消息ClusterEvent.CurrentClusterState,得到当前的所有客户端;并向每一个客户端注册自己,到客户端容器中.
 * 总的说来:
 *      每一个客户端保存了所有服务器段的信息;由每一个客户端来维护服务器端信息.
 *      每一个服务器端向客户端注册自己.
 *
 * @Refer To: https://blog.csdn.net/beliefer/article/details/53893929
 * https://blog.csdn.net/liubenlong007/article/details/54603296
 */
public class MyAkkaClusterServer extends UntypedActor {

    LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    Cluster cluster = Cluster.get(getContext().system());

    // subscribe to cluster changes
    @Override
    public void preStart() {
        // #subscribe
        //服务端自己订阅了集群的成员上线消息；
        cluster.subscribe(getSelf(), ClusterEvent.MemberUp.class);
        // #subscribe
    }

    // re-subscribe when restart
    @Override
    public void postStop() {
        cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) {
        if (message instanceof TransformationMessages.TransformationJob) {
            TransformationMessages.TransformationJob job = (TransformationMessages.TransformationJob) message;
            logger.info(job.getText());
            getSender().tell(new TransformationMessages.TransformationResult(job.getText().toUpperCase()), getSelf());

        } else if (message instanceof ClusterEvent.CurrentClusterState) {
            /**
             * 当前节点在刚刚加入集群时，会收到CurrentClusterState消息，从中可以解析出集群中的所有前端节点（即roles为frontend的），并向其发送BACKEND_REGISTRATION消息，用于注册自己
             */
            ClusterEvent.CurrentClusterState state = (ClusterEvent.CurrentClusterState) message;
            for (Member member : state.getMembers()) {
                if (member.status().equals(MemberStatus.up())) {
                    register(member);
                }
            }

        } else if (message instanceof ClusterEvent.MemberUp) {
            /**
             * 有新的节点加入
             */
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            register(mUp.member());

        } else {
            unhandled(message);
        }

    }

    /**
     * 如果是客户端角色，则像客户端注册自己的信息。客户端收到消息以后会讲这个服务端存到本机服务列表中
     * @param member
     */
    void register(Member member) {
        if (member.hasRole("client"))
            getContext().actorSelection(member.address() + "/user/myAkkaClusterClient").tell(BACKEND_REGISTRATION, getSelf());
    }

    public static void main(String [] args){
        System.out.println("Start MyAkkaClusterServer");
        ActorSystem system = ActorSystem.create("akkaClusterTest", ConfigFactory.load("server.conf"));
        system.actorOf(Props.create(MyAkkaClusterServer.class), "myAkkaClusterServer");
        System.out.println("Started MyAkkaClusterServer");

    }
}