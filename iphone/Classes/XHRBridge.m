/**
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */

#import "XHRBridge.h"
#import "TiHost.h"
#import "TiProxy.h"
#import "SBJSON.h"

static XHRBridge *xhrBridge = nil;

@implementation TiProtocolHandler

+ (NSString*) specialProtocolScheme 
{
	return @"ti";
}

+ (void) registerSpecialProtocol 
{
	static BOOL inited = NO;
	if ( ! inited ) 
	{
		[NSURLProtocol registerClass:[TiProtocolHandler class]];
		inited = YES;
	}
}

+ (BOOL)canInitWithRequest:(NSURLRequest *)theRequest 
{
	NSString *theScheme = [[theRequest URL] scheme];
	return [theScheme isEqual:[self specialProtocolScheme]];
}

+ (BOOL)requestIsCacheEquivalent:(NSURLRequest *)a toRequest:(NSURLRequest *)b;
{
	return [super requestIsCacheEquivalent:a toRequest:b];
}

+(NSURLRequest *)canonicalRequestForRequest:(NSURLRequest *)request 
{
    return request;
}

- (void)startLoading
{
	id<NSURLProtocolClient> client = [self client];
    NSURLRequest *request = [self request];
	NSURL *url = [request URL];
	
	NSString *pageToken = [url host];
	NSArray *parts = [[[url path] substringFromIndex:1] componentsSeparatedByString:@"/"];
	NSString *operation = [parts objectAtIndex:0];
	NSString *moduleName = [parts objectAtIndex:1];
	NSString *property = [parts objectAtIndex:2];
	NSString *exception = nil;
	
	NSString *prequery = [url query];
	NSString *arguments = prequery==nil ? @"" : [prequery stringByReplacingPercentEscapesUsingEncoding:NSUTF8StringEncoding];
	
	NSLog(@"OP = %@",operation);
	NSLog(@"MODULE = %@",moduleName);
	NSLog(@"PROP = %@",property);
	NSLog(@"ARGS = %@",arguments);
	
	NSData *data = [xhrBridge invoke:pageToken operation:operation module:moduleName property:property arguments:arguments exception:&exception];
	
	if (data!=nil)
	{
		NSURLCacheStoragePolicy caching = NSURLCacheStorageNotAllowed;
		NSURLResponse *response = [[NSURLResponse alloc] initWithURL:url MIMEType:@"text/javascript" expectedContentLength:[data length] textEncodingName:@"utf-8"];
		[client URLProtocol:self didReceiveResponse:response cacheStoragePolicy:caching];
		[client URLProtocol:self didLoadData:data];
		[client URLProtocolDidFinishLoading:self];
		[response release];
	}
	else 
	{
		NSLog(@"Error loading %@",url);
		[client URLProtocol:self didFailWithError:[NSError errorWithDomain:NSURLErrorDomain code:NSURLErrorResourceUnavailable userInfo:nil]];
		[client URLProtocolDidFinishLoading:self];
	}
}

- (void)stopLoading 
{
}

@end


@implementation XHRBridge

-(id)init
{
	if (self = [super init])
	{
		xhrBridge = self;
	}
	return self;
}

-(void)boot:(id)callback url:(NSURL*)url preload:(NSDictionary*)preload
{
	[TiProtocolHandler registerSpecialProtocol];
}

-(void)shutdown
{
}

-(void)gc
{
}

@end