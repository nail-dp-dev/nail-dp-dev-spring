// var stompClient = null;
//
// function setConnected(connected) {
//     $("#connect").prop("disabled", connected);
//     $("#disconnect").prop("disabled", !connected);
//     if (connected) {
//         $("#conversation").show();
//     } else {
//         $("#conversation").hide();
//     }
//     $("#messages").html("");
// }
//
// function connect() {
//     var socket = new SockJS('/ws-stomp');
//     stompClient = Stomp.over(socket);
//     stompClient.connect({}, function (frame) {
//         setConnected(true);
//         console.log('Connected: ' + frame);
//         stompClient.subscribe('/sub/chat/9283e8c1-2a30-45e1-9da3-e4e395e87d89', function (message) {
//             showMessage(JSON.parse(message.body));
//         });
//     });
// }
//
// function disconnect() {
//     if (stompClient !== null) {
//         stompClient.disconnect();
//     }
//     setConnected(false);
//     console.log("Disconnected");
// }
//
// function sendMessage() {
//     const content = [$("#message").val()];
//     const sender = $("#sender").val();
//     const mention = $("#mention").val() ? [$("#mention").val()] : [];
//     const messageType = "TEXT";
//
//     const chatMessageDto = {
//         content: content,
//         sender: sender,
//         mention: mention,
//         messageType: messageType
//     };
//
//     stompClient.send("/pub/chat/9283e8c1-2a30-45e1-9da3-e4e395e87d89/message", {}, JSON.stringify(chatMessageDto));
// }
//
// function showMessage(message) {
//     $("#messages").append("<tr><td><strong>" + message.sender + ":</strong> " + message.content.join(', ') + "</td></tr>");
// }
//
// $(function () {
//     $("form").on('submit', function (e) {
//         e.preventDefault();
//     });
//     $("#connect").click(function () {
//         connect();
//     });
//     $("#disconnect").click(function () {
//         disconnect();
//     });
//     $("#send").click(function () {
//         if (stompClient && stompClient.connected) {
//             sendMessage();
//         } else {
//             alert("WebSocket is not connected.");
//         }
//     });
// });