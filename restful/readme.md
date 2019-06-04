#restful web service

Rest( REpresentation State Transfer), **resource** representation state transfer

- resource: 任何可以被使用的事物都可以作为资源
    
    - 资源名称: URI(uniform resource identifier)
    - 主要特点: 可寻址性, 无状态性, 连通性, 接口统一性
- representation: json, xml, jpeg
- state transfer: 状态变化

    - 通过HTTP动作实现: get, post, put, delete, head, options等
    

rest是一种软件风格架构,包含一组架构约束条件和原则(不是标准!),满足这些条件和原则的应用就是restful的:

- Client-Server: 服务器与客户端分离
- stateless: 来自客户的每个请求 都需要包含服务器处理该请求所需的全部信息
(服务器不能存储来自客户的某个请求的信息,并在该客户的其他定球中使用), 适用于云计算环境, 
- uniform interface: 统一接口, 客户与服务器之间的通信方法必须是统一的,如get, post, put, delete等





