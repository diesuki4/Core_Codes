import grpc
from concurrent import futures
import time
import match_pb2_grpc as pb2_grpc
import match_pb2 as pb2
from sortedcollections import ValueSortedDict

USE_SSL = True
SSL_PRIVATE_KEY_PATH = 'auth/my.server.com.key'
SSL_CERTIFICATE_PATH = 'auth/my.server.com.crt'

def print_status(vsd) :

    print('----------------------')
    for k, v in vsd.items() :
        print('[', k, ']', '=', -v)
    print('----------------------')
    print('')

class MatchService(pb2_grpc.MatchServicer):

    # Value 로 정렬되는 딕셔너리
    # {DS 주소: 현재 인원} 저장
    vsd = ValueSortedDict()

    def __init__(self, *args, **kwargs) :
        pass

    # 클라이언트(플레이어) 가 접속할 서버 반환
    def GetDestination(self, request, context) :

        if self.vsd :
            result = {'matched_server': self.vsd.peekitem(0)[0], 'success': True}
            print('GetDestination():', self.vsd.peekitem(0)[0])
            return pb2.MatchResponse(**result)
        else :
            result = {'matched_server': '', 'success': False}
            print('GetDestination(): False')
            return pb2.MatchResponse(**result)

    # 딕셔너리에서 해당 DS 의 현재 인원 갱신
    def UpdateToMatchmaker(self, request, context) :

        # 인원이 많을수록 앞(높은 우선순위) 쪽에 오게 하기 위해 음수로 저장
        self.vsd[request.server_address] = -request.num_current_clients

        if self.vsd[request.server_address] == -request.num_current_clients :
            result = {'success': True}
            print('UpdateToMatchmaker(): [', request.server_address, ']', '=', request.num_current_clients)
            print_status(self.vsd)
            return pb2.UpdateResponse(**result)
        else :
            result = {'success': False}
            print('UpdateToMatchmaker(): False')
            print_status(self.vsd)
            return pb2.UpdateResponse(**result)

    # 딕셔너리에서 DS 주소 삭제
    def RemoveFromMatchmaker(self, request, context) :

        if self.vsd.get(request.server_address) :
            self.vsd.pop(request.server_address)
            result = {'success': True}
            print('RemoveFromMatchmaker():', request.server_address)
            print_status(self.vsd)
            return pb2.RemoveResponse(**result)
        else :
            result = {'success': False}
            print('RemoveFromMatchmaker(): False')
            print_status(self.vsd)
            return pb2.RemoveResponse(**result)

def serve():
    
    server = grpc.server(futures.ThreadPoolExecutor(max_workers = 4))
    pb2_grpc.add_MatchServicer_to_server(MatchService(), server)

    if USE_SSL :
        with open(SSL_PRIVATE_KEY_PATH, 'rb') as f:
            private_key = f.read()
        with open(SSL_CERTIFICATE_PATH, 'rb') as f:
            certificate_chain = f.read()
        server_credentials = grpc.ssl_server_credentials(((private_key, certificate_chain,),),)
        server.add_secure_port('[::]:11111', server_credentials)
    else :
        server.add_insecure_port('[::]:11111')

    server.start()
    server.wait_for_termination()

if __name__ == '__main__' :
    serve()
