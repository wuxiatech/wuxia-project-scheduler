package cn.wuxia.project.scheduler;

import org.springframework.stereotype.Component;

@Component
public class AllPushMessageService {

   public void enforceAllPush(AllPushMessage allPushMessage){
       System.out.println(allPushMessage);
   }
}
